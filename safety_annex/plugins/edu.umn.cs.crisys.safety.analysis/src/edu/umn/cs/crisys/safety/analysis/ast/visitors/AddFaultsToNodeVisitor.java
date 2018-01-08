package edu.umn.cs.crisys.safety.analysis.ast.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.osate.aadl2.instance.ComponentInstance;

import com.rockwellcollins.atc.agree.analysis.AgreeUtils;
import com.rockwellcollins.atc.agree.analysis.ast.AgreeEquation;
import com.rockwellcollins.atc.agree.analysis.ast.AgreeNode;
import com.rockwellcollins.atc.agree.analysis.ast.AgreeNodeBuilder;
import com.rockwellcollins.atc.agree.analysis.ast.AgreeProgram;
import com.rockwellcollins.atc.agree.analysis.ast.AgreeStatement;
import com.rockwellcollins.atc.agree.analysis.ast.AgreeVar;
import com.rockwellcollins.atc.agree.analysis.ast.visitors.AgreeASTMapVisitor;

import edu.umn.cs.crisys.safety.analysis.SafetyException;
import edu.umn.cs.crisys.safety.analysis.transform.Fault;
import edu.umn.cs.crisys.safety.analysis.transform.FaultASTBuilder;
import edu.umn.cs.crisys.safety.safety.AnalysisBehavior;
import edu.umn.cs.crisys.safety.safety.AnalysisStatement;
import edu.umn.cs.crisys.safety.safety.FaultCountBehavior;
import edu.umn.cs.crisys.safety.safety.FaultStatement;
import edu.umn.cs.crisys.safety.safety.TemporalConstraint;
import edu.umn.cs.crisys.safety.safety.TransientConstraint;
import edu.umn.cs.crisys.safety.safety.PermanentConstraint;
import edu.umn.cs.crisys.safety.safety.ProbabilityBehavior;
import edu.umn.cs.crisys.safety.safety.SpecStatement;
import edu.umn.cs.crisys.safety.util.RecordIdPathElement;
import edu.umn.cs.crisys.safety.util.SafetyUtil;
import jkind.lustre.BinaryExpr;
import jkind.lustre.BinaryOp;
import jkind.lustre.Expr;
import jkind.lustre.IdExpr;
import jkind.lustre.IfThenElseExpr;
import jkind.lustre.IntExpr;
import jkind.lustre.NamedType;
import jkind.lustre.Node;
import jkind.lustre.NodeCallExpr;
import jkind.lustre.RecordAccessExpr;
import jkind.lustre.RecordUpdateExpr;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;
import jkind.lustre.VarDecl;


// If agreeNode is non-null, then we scope the replacement to only occur in one node.
// Otherwise, we do id replacement across all nodes.

public class AddFaultsToNodeVisitor extends AgreeASTMapVisitor {

	// List of Lustre nodes.
	private List<Node> globalLustreNodes;
	private AgreeNode topNode;
	
	// What does the key represent for the following?
	private Map<String, List<String>> faultyVars = new HashMap<String, List<String>>();
	private Map<String, List<Pair<Expr,Fault>>> faultyVarsExpr = new HashMap<String, List<Pair<Expr,Fault>>>();
	private Map<String, String> theMap = new HashMap<>();
	private Map<Fault, List<String>> mapFaultToLustreNames = new HashMap<Fault, List<String>>();
	private Map<Fault, String> mapFaultToPath = new HashMap<>();
	
	// Fault map: stores the faults associated with a node.
	// Keying off component instance rather than AgreeNode, just so we don't
	// have problems with "stale" AgreeNode references during transformations.

	// It is used to properly set up the top-level node for triggering faults. 
	private Map<ComponentInstance, List<Fault>> faultMap = new HashMap<>(); 
	
	public Map<ComponentInstance, List<Fault>> getFaultMap() {
		return faultMap;
	}
	
	public AddFaultsToNodeVisitor() {
		super(new jkind.lustre.visitors.TypeMapVisitor());
	}

	@Override
	public AgreeProgram visit(AgreeProgram program) {
		globalLustreNodes = new ArrayList<>(program.globalLustreNodes);
		this.topNode = program.topNode;  
		
		// do not call back to 'super'.  This is BROKEN!
		AgreeNode topNode = this.visit(program.topNode);

		program = new AgreeProgram(program.agreeNodes, globalLustreNodes, program.globalTypes, topNode, program.containsRealTimePatterns);
		return program;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// AGREENODE TRAVERSAL STARTS HERE.
	// 
	// since we don't totally know the traversal order, if we only
	// want to replace in a single node, we need to store the 
	// 'stacked' inNode, then restore it after a traversal.
	//
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public AgreeNode visit(AgreeNode node) {
		Map<String, List<String>> oldFaultyVars = faultyVars;

		boolean isTop = (node == this.topNode);
		List<Fault> faults = gatherFaults(globalLustreNodes, node, isTop); 
		faults = renameFaultEqs(faults);
		
		if (faultMap.containsKey(node.compInst)) {
			System.out.println("Node: " + node.id + " has already been visited!");
			throw new SafetyException("Node: " + node.id + " has been visited twice during AddFaultsToNodeVisitor!");
		}
		faultMap.put(node.compInst, faults);

		faultyVars = gatherFaultyOutputs(faults, node);
		node = super.visit(node);

		AgreeNodeBuilder nb = new AgreeNodeBuilder(node);
		addNominalVars(node, nb); 
		addFaultInputs(faults, nb);
		addFaultLocalEqsAndAsserts(faults, nb); 
		addFaultNodeEqs(faults, nb); 

		if (isTop) {
			topNode = node;
			AnalysisBehavior maxFaults = this.gatherTopLevelFaultCount(node);
			addTopLevelFaultDeclarations(node, new ArrayList<>(), nb);
			addTopLevelFaultOccurrenceConstraints(maxFaults, node, nb);
		}
		
		node = nb.build();		
		
		faultyVars = oldFaultyVars;
		return node;
	}
	
	public void addNominalVars(AgreeNode node, AgreeNodeBuilder nb) {
		// Get key (faultyId string = root) and iterate through list of paths (faultPath)
		// Create new nominal variables for each pair (root.path). 
		for (String faultyId : faultyVars.keySet()) {
			for(String faultPath: faultyVars.get(faultyId)) {
			  AgreeVar out = findVar(node.outputs, (faultyId));
		  	  nb.addInput(new AgreeVar(createNominalId((faultyId+"."+faultPath)), out.type, out.reference));
			}
		}
	}

	public void addFaultInputs(List<Fault> faults, AgreeNodeBuilder nb) {
		for (Fault f: faults) {
			nb.addInput(new AgreeVar(createFaultNodeInputId(f.id), NamedType.BOOL, f.faultStatement));
		}
	}
	
	public void addFaultLocalEqsAndAsserts(List<Fault> faults, AgreeNodeBuilder nb) {
		for (Fault f: faults) {
			nb.addInput(f.safetyEqVars);
			nb.addAssertion(f.safetyEqAsserts);
		}
	}

	public List<Expr> constructNodeInputs(Fault f) {
		List<Expr> actuals = new ArrayList<>(); 
		for (VarDecl vd: f.faultNode.inputs) {
			// there is an extra "trigger" input 
			Expr actual ;
			if (vd.id.equalsIgnoreCase("trigger")) {
				if (f.faultInputMap.containsKey(vd.id)) {
					throw new SafetyException("Trigger input for fault node should not be explicitly assigned by user.");
				}
				actual = new IdExpr(createFaultNodeInputId(f.id));
			} else {
				actual = f.faultInputMap.get(vd.id);
				
				// do any name conversions on the stored expression.
				actual = actual.accept(this);
				
				if (actual == null) {
					throw new SafetyException("fault node input: '" + vd.id + "' is not assigned.");					
				}
			}
			actuals.add(actual);
		}
		return actuals;
	}
	
	public void addFaultNodeEqs(List<Fault> faults, AgreeNodeBuilder nb) {
		for (Fault f: faults) {
			List<IdExpr> lhs = new ArrayList<IdExpr>();
			for (VarDecl v: f.faultNode.outputs) {
				String lhsId = this.createFaultNodeEqId(f.id, v.id);
				nb.addLocal(new AgreeVar(lhsId, v.type, f.faultStatement));
				lhs.add(new IdExpr(lhsId));
			}
			
			AgreeEquation eq = new AgreeEquation(lhs, 
					new NodeCallExpr(f.faultNode.id, constructNodeInputs(f)), f.faultStatement);
			nb.addLocalEquation(eq);
			
			// CHANGE THIS!  
			for(Map.Entry<String, Expr> outMap: f.faultOutputMap.entrySet()) {
				String lhsId = this.createFaultNodeEqId(f.id,  outMap.getKey());
				nb.addAssertion(
					new AgreeStatement("",
						new BinaryExpr(new IdExpr(lhsId), BinaryOp.EQUAL, outMap.getValue()),
						f.faultStatement));
			}
			

			
		}
		// Binding happens HERE and is based on the map from Id -> list of Expr.
		// Create an equality between the id and a nested WITH expression for each expr 
		// in the list.
		
		for(String root : faultyVarsExpr.keySet()){
			// so: root will be the LHS of the equality
			// Expr base = createNominalId(e.id);
			// base is the root of the WITH expression.
			
			
			List<Pair<Expr,Fault>> list = faultyVarsExpr.get(root);
			
			String nomId = createNominalId(root);
			Expr base = new IdExpr(nomId);
			
			for(Pair<Expr, Fault> pair : list) {
				
				List<RecordIdPathElement> path = AgreeUtils.getExprPath(new ArrayList<RecordIdPathElement>(), pair.ex);
				// what we want here is to replace 'base' with a new 'base' based on the fault path replacement.
				// In utils, we have createNestedUpdateExpr(root, path, repl)
				// We have 1. Root (base).  We have path (path).
				// What wwe need is repl.  This is the LHS of some fault node equation - how do we find it?
				// The issue involves faults that assign more than one output...so just having the fault may not be 
				// enough - we need to know the fault and which field is relevant for this assignment.
				
			}
			
			
		}
	}
	
	/*
	 * Method used to create nested with statements
	 */
//	private List<Expr> createNestedWithStmt(List<Pair<Expr, Fault>> list) {
//
//		// Create new list for return values
//		List<Expr> returnList = new ArrayList<Expr>();
//		
//		for(Pair<Expr,Fault> pair: list) {
//			List<String> recs = AgreeUtils.getExprPath(new ArrayList<String>(), pair.ex);
//			
//			
//		}
//		
//		return returnList;
//	}
	
	public Map<String, String> constructEqIdMap(Fault f, List<AgreeVar> eqVars) {
		theMap = new HashMap<>(); 
		for (AgreeVar eqVar: eqVars) {
			// ComponentInstance ci = eqVar.compInst;
			theMap.put(eqVar.id, createFaultEqId(f.id, eqVar.id));
			
//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
//			System.out.println("Key : value = "+name + " : "+createFaultEqId(f.id, name));
//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
		}
		return theMap;
	}
	
	public Fault renameEqId(Fault f, Map<String, String> idMap) {
		Fault newFault = new Fault(f);
		newFault.safetyEqVars.clear();
		newFault.safetyEqAsserts.clear();
		newFault.faultOutputMap.clear();
		newFault.faultInputMap.clear();
		
		if (!f.triggers.isEmpty()) {
			throw new SafetyException("Triggers are currently unsupported for translation");
		}
		
		// update the variable declarations
		for (AgreeVar eq: f.safetyEqVars) {
			if (idMap.containsKey(eq.id)) {
				eq = new AgreeVar(createFaultEqId(f.id, eq.id), eq.type, eq.reference);
			}
			newFault.safetyEqVars.add(eq); 
		}
		
		ReplaceIdVisitor visitor = new ReplaceIdVisitor(idMap);
		for (AgreeStatement s: f.safetyEqAsserts) {
			newFault.safetyEqAsserts.add(visitor.visit(s));
		}
		
		for (Map.Entry<String, Expr> element: f.faultOutputMap.entrySet()) {
			newFault.faultOutputMap.put(element.getKey(), visitor.visit(element.getValue()));
		}
		
		for (Map.Entry<String, Expr> element: f.faultInputMap.entrySet()) {
			newFault.faultInputMap.put(element.getKey(), element.getValue().accept(visitor));
		}
		
		return newFault;
	}
	
	public List<Fault> renameFaultEqs(List<Fault> faults) {
		List<Fault> newFaults = new ArrayList<>(); 
		for (Fault f: faults) {
			Map<String, String> idMap = constructEqIdMap(f, f.safetyEqVars);
			newFaults.add(this.renameEqId(f, idMap));
		}
		return newFaults;
	}
	
	
	@Override
	public Expr visit(IdExpr e) {
		if (faultyVars.containsKey(e.id)) {	
			return new IdExpr(e.location, createNominalId(e.id));
			
		} else {
			return e;
		}
	}

	public static AgreeVar findVar(List<AgreeVar> vars, String id) {
		for (AgreeVar v: vars) {
			if (v.id.equals(id)) {
				return v;
			}
		}
		return null;
	}
	
	/*
	private Map<String, List<String>> gatherFaultyOutputs(List<Fault> faults, AgreeNode node) {
		Map<String, List<String>> outputSet = new HashMap<String, List<String>>(); 
		RecordAccessExpr recordExpr = null;
		IdExpr idExpr = null;
		String id = "";
		List<RecordIdPathElement> idPath = new ArrayList<>();
		String finalPath = "";
		for (Fault f: faults) {
			for (Expr ide: f.faultOutputMap.values()) {
				
				if(ide instanceof IdExpr) {
					idExpr = (IdExpr) ide;
					id = idExpr.id;
					idPath = AgreeUtils.getExprPath(new ArrayList<RecordIdPathElement>(), idExpr);
					addIdToMap(ide, id, f);
				} else if(ide instanceof RecordAccessExpr) {
					recordExpr = (RecordAccessExpr) ide;
					id = recordExpr.record.toString();
					addIdToMap(ide, id, f);
				} else {
					throw new SafetyException("Error: Can only handle IdExpr and "+
							"Record Access Expressions.");
				}
				// Check for id expression match. 
				if (outputSet.containsKey(id)) {
					// Concatenate idPath into '.' form
					for(String part: idPath) {
						finalPath = finalPath + "." + part;
					}
					outputSet.get(id).add(finalPath);
					
				} else {
					outputSet.put(id, idPath);
				}
			}
		}
		return outputSet;
	}
	*/
	
	private void addIdToMap(Expr ex, String id, Fault f) {
		Pair<Expr,Fault> pair = new Pair<Expr, Fault>(ex, f);
		if(faultyVarsExpr.containsKey(id)) {
			faultyVarsExpr.get(id).add(pair);
		}
		else {
			List<Pair<Expr,Fault>> list = new ArrayList<Pair<Expr,Fault>>();
			list.add(pair);
			faultyVarsExpr.put(id, list);
		}

	}
	
	public String createFaultEventId(String base) {
		return "__fault__event__" + base;		
	}
	
	public String createFaultActiveId(String base) {
		return "__fault__active__" + base;		
	}
	
	public String createFaultNodeInputId(String base) {
		return "fault__trigger__" + base;		
	}
	
	public String createNominalId(String output) {
		return "__fault__nominal__" + output;
	}

	public String createFaultEqId(String fault, String var) {
		return fault + "__" + var; 
	}
	public String createFaultNodeEqId(String fault, String var) {
	
		return fault + "__node__" + var; 
	}
	
	public List<Fault> gatherFaults(List<Node> globalLustreNodes, AgreeNode node, boolean isTop) {
		List<SpecStatement> specs = 
			SafetyUtil.collapseAnnexes(
				SafetyUtil.getSafetyAnnexes(node, isTop));
		
		List<Fault> faults = new ArrayList<>(); 
		for (SpecStatement s : specs) {
			if (s instanceof FaultStatement) {
				FaultStatement fs = (FaultStatement)s;
				FaultASTBuilder builder = new FaultASTBuilder(globalLustreNodes, node);
				Fault safetyFault = builder.buildFault(fs);
				faults.add(safetyFault); 
			}
		}
		return faults;
	}

	public AnalysisBehavior gatherTopLevelFaultCount(AgreeNode node) {
		AnalysisBehavior ab = null;
		boolean found = false;

		List<SpecStatement> specs = 
			SafetyUtil.collapseAnnexes(
				SafetyUtil.getSafetyAnnexes(node, true));
		 
		for (SpecStatement s : specs) {
			if (s instanceof AnalysisStatement) {
				AnalysisStatement as = (AnalysisStatement)s;
				ab = as.getBehavior();
				if (ab instanceof FaultCountBehavior) {
					int maxFaults = Integer.valueOf(((FaultCountBehavior)ab).getMaxFaults());
					if (maxFaults < 0) {
						throw new SafetyException("Maximum number of faults must be non-negative.");
					}
				} else if (ab instanceof ProbabilityBehavior) {
					double minProbability = Double.valueOf(((ProbabilityBehavior)ab).getProbabilty());
					if (minProbability > 1 || minProbability < 0) {
						throw new SafetyException("Probability out of range [0, 1]");
					}
				}
				if (found) {
					throw new SafetyException("Multiple analysis specifications found.  Only one can be processed");
				}
				found = true;
			} 
		}
		if (!found) {
			throw new SafetyException("No analysis statement; unable to run safety analysis");
		}
		return ab;
	}
	
	
	/*
	 * 1. For each subcomponent node
		For each subcomponent fault (depth-first)
			0. Perform a traversal to find all the node/fault pairs
			1a. Define an unconstrained local eq. to represent each fault-event 
			1b. Define a constrained local eq. to assign fault-active value depending on 
				fault duration in node.
			1c. Assign subcomponent fault input to fault-active eq with assertions (yay!) 
	(test: print updated AST)
		2. Assign faults-active equation to sum of all fault-active values
			(test: print updated AST)
		3. Assert that this value is <= 1 (FOR NOW!)	
			(test: print updated AST)
		4. Use shiny new fault annex to perform safety analysis
			(test: analysis results)
	 */

	
	public String addPathDelimiters(List<String> path, String var) {
		String id = ""; 
		for (String p: path) {
			id = id + p + "__";
		}
		return id + var;
	}

	public Expr createPermanentExpr(Expr varId, Expr expr) {
		Expr latch = 
			new BinaryExpr(expr, BinaryOp.ARROW, 
				new BinaryExpr(expr, BinaryOp.OR, 
					new UnaryExpr(UnaryOp.PRE, varId)));
		Expr equate = 
			new BinaryExpr(varId, BinaryOp.EQUAL, latch);
		
		return equate;
	}
	
	public Expr createTransientExpr(Expr varId, Expr expr) {
		Expr equate = 
				new BinaryExpr(varId, BinaryOp.EQUAL, expr);
		return equate;
	}
	
	public void constrainFaultActive(Fault f, String nameBase, AgreeNodeBuilder builder) {
		IdExpr eventExpr = new IdExpr(this.createFaultEventId(nameBase));
		IdExpr activeExpr = new IdExpr(this.createFaultActiveId(nameBase));
		Expr assertExpr;
		
		TemporalConstraint tc = f.duration.getTc();
		if (tc instanceof PermanentConstraint) {
			assertExpr = createPermanentExpr(activeExpr, eventExpr); 
		} else if (tc instanceof TransientConstraint){
			System.out.println("WARNING: ignoring duration on transient faults");
			assertExpr = createTransientExpr(activeExpr, eventExpr);
		} else {
			throw new SafetyException("Unknown constraint type during translation of fault "+ f.id);
		}
		builder.addAssertion(new AgreeStatement("", assertExpr, f.faultStatement));
	}
	
	/*
	 * Create Lustre name using addPathDelimiters
	 * Create map from fault to the constructed Lustre name 
	 * 		(used in renaming)
	 * Add in the __fault__active__ portion of the Lustre name
	 * Add assertion to the builder with the fault statement equated to the 
	 * 		active var id.
	 */
	public void mapFaultActiveToNodeInterface(Fault f, List<String> path, String base, AgreeNodeBuilder builder) {
		String interfaceVarId = addPathDelimiters(path, this.createFaultNodeInputId(f.id));
		String activeVarId = this.createFaultActiveId(base);
		
		// Create map from fault to it's relative path
		// for the counterexample layout
		for(String str : path) {	
			mapFaultToPath.put(f, str);
		}
		
		Expr equate = new BinaryExpr(new IdExpr(interfaceVarId), BinaryOp.EQUAL, new IdExpr(activeVarId));
		builder.addAssertion(new AgreeStatement("", equate, f.faultStatement));
	}
	
	
	public void addTopLevelFaultDeclarations(
			AgreeNode currentNode, 
			List<String> path, 
			AgreeNodeBuilder nb) {
		
		List<Fault> faults = this.faultMap.get(currentNode.compInst) ; 
		
		// Add unconstrained input and constrained local to represent fault event and 
		// whether or not fault is currently active.
		for (Fault f: faults) {
			String base = addPathDelimiters(path, f.id);
			nb.addInput(new AgreeVar(this.createFaultEventId(base), NamedType.BOOL, f.faultStatement));
			nb.addInput(new AgreeVar(this.createFaultActiveId(base), NamedType.BOOL, f.faultStatement));
			
			if(mapFaultToLustreNames.containsKey(f)) {
				mapFaultToLustreNames.get(f).add(this.createFaultActiveId(base));
			}
			else {
				List<String> names = new ArrayList<>();
				names.add(this.createFaultActiveId(base));
				mapFaultToLustreNames.put(f, names);
			}

			// constrain fault-active depending on transient / permanent & map it to a 
			// fault in the node interface
			constrainFaultActive(f, base, nb);
			mapFaultActiveToNodeInterface(f, path, base, nb);
		}
		
		for (AgreeNode n: currentNode.subNodes) {
			List<String> ext = new ArrayList<>(path);
			ext.add(n.id);
			addTopLevelFaultDeclarations(n, ext, nb);
		}
	}

	
	/*****************************************************************
	 * 
	 * # of occurrence-based fault calculations
	 * 
	 ******************************************************************/
	
	public Expr createSumExpr(Expr cond) {
		return new IfThenElseExpr(cond, new IntExpr(1), new IntExpr(0));
	}
	
	public void getFaultCountExprList(
			AgreeNode currentNode, 
			List<String> path, 
			List<Expr> sumExprs) {

		List<Fault> faults = this.faultMap.get(currentNode.compInst) ; 
		for (Fault f: faults) {
			String base = addPathDelimiters(path, f.id);
			sumExprs.add(
				createSumExpr(new IdExpr(this.createFaultActiveId(base))));
		}
		
		for (AgreeNode n: currentNode.subNodes) {
			List<String> ext = new ArrayList<>(path);
			ext.add(n.id);
			getFaultCountExprList(n, ext, sumExprs);
		}
	}
	
	public Expr buildFaultCountExpr(List<Expr> exprList, int index) {
		if (index > exprList.size() - 1) {
			return new IntExpr(0);
		}
		else if (index == exprList.size() - 1) {
			return exprList.get(index);
		} else {
			return new BinaryExpr(exprList.get(index), BinaryOp.PLUS, 
					buildFaultCountExpr(exprList, index+1));
		}
	}
	
	public void addTopLevelMaxFaultOccurrenceConstraint(
			int maxFaults,
			AgreeNode topNode,
			AgreeNodeBuilder builder) {
		
		// add a global fault count
		String id = "__fault__global_count"; 
		builder.addInput(new AgreeVar(id, NamedType.INT, topNode.reference));

		// assign it.
		List<Expr> sumExprs = new ArrayList<>(); 
		getFaultCountExprList(topNode, new ArrayList<>(), sumExprs);
		Expr faultCountExpr = buildFaultCountExpr(sumExprs, 0);
		Expr equate = 
			new BinaryExpr(new IdExpr(id), BinaryOp.EQUAL, faultCountExpr);
		builder.addAssertion(new AgreeStatement("", equate, topNode.reference));

		// assert that the value is <= 1
		Expr lessEqual = 
			new BinaryExpr(new IdExpr(id), BinaryOp.LESSEQUAL, new IntExpr(maxFaults));
		builder.addAssertion(new AgreeStatement("", lessEqual, topNode.reference));
		
		// and Viola!
	}
	
	/*****************************************************************
	 * 
	 * probability-based fault calculations
	 * 
	 ******************************************************************/
	class FaultProbability implements Comparable<FaultProbability>{
		public double probability;
		public String faultName;
		
		public FaultProbability(String faultName, double probability) {
			this.probability = probability;
			this.faultName = faultName;
		}

		@Override
		public int compareTo(FaultProbability o) {
			// Want to sort LARGEST first, so negate probabilities.
			return Double.compare(-probability,  -o.probability);
		}
		
		public String toString() {
			return "(" + probability + ", " + faultName + ")";
		}
	}; 
	
	class FaultSetProbability implements Comparable<FaultSetProbability> {
		// invariant: probability should equal the multiple of all probabilities in elements.
		public double probability;
		public Set<FaultProbability> elements;

		public FaultSetProbability(double probability, FaultProbability fp) {
			this.probability = probability;
			this.elements = Collections.singleton(fp);
		}
		
		public FaultSetProbability(double probability, FaultSetProbability base, FaultProbability fp) {
			this.probability = probability; 
			this.elements = new HashSet<>(base.elements);
			this.elements.add(fp);
		}

		@Override
		public int compareTo(FaultSetProbability o) {
			// Want to sort LARGEST first, so negate probabilities.
			return Double.compare(-probability,  -o.probability);
		}
		
		public String toString() {
			return "(" + probability + ", " + elements.toString() + " )";
		}
	}
	
	public void getFaultProbExprList(
			AgreeNode currentNode, 
			List<String> path, 
			List<FaultProbability> probabilities) {

		List<Fault> faults = this.faultMap.get(currentNode.compInst) ; 
		for (Fault f: faults) {
			String base = addPathDelimiters(path, f.id);
			probabilities.add(new FaultProbability(this.createFaultActiveId(base), f.probability));
		}
		
		for (AgreeNode n: currentNode.subNodes) {
			List<String> ext = new ArrayList<>(path);
			ext.add(n.id);
			getFaultProbExprList(n, ext, probabilities);
		}
	}
	
	public Expr getNoFaultProposition(Set<FaultProbability> elements) {
		Expr noFaultExpr = null;  
		for (FaultProbability fp: elements) {
			Expr local = new UnaryExpr(UnaryOp.NOT, new IdExpr(fp.faultName));
			if (noFaultExpr == null) {
				noFaultExpr = local;
			} else {
				noFaultExpr = new BinaryExpr(local, BinaryOp.AND, noFaultExpr);
			}
		}
		assert (noFaultExpr != null);
		return noFaultExpr;
	}
	
	
	public void addTopLevelMaxFaultOccurrenceConstraint(
			double minProbability, 
			AgreeNode topNode, 
			AgreeNodeBuilder builder) {
		
		ArrayList<FaultProbability> elementProbabilities = new ArrayList<>();
		ArrayList<FaultSetProbability> faultCombinationsAboveThreshold = new ArrayList<>();
		PriorityQueue<FaultSetProbability> pq = new PriorityQueue<>();

		// gather all fault probabilities.  If there are no faults, exit out.
		getFaultProbExprList(topNode, new ArrayList<>(), elementProbabilities);
		Collections.sort(elementProbabilities);		
		if (elementProbabilities.isEmpty()) {
			return;
		}
		
		System.out.println("elementProbabilities: " + elementProbabilities); 
		
		// remove elements from list that are too unlikely & add remaining elements to 
		// 'good' set.  
		ArrayList<FaultProbability> remainder = new ArrayList<>(elementProbabilities);
		for (int i=0; i < remainder.size(); i++) {
			FaultProbability elementProbability = remainder.get(i);
			if (elementProbability.probability < minProbability) {
				remainder.subList(i, remainder.size()).clear();
			}
			else {
				FaultSetProbability fsp = 
					new FaultSetProbability(elementProbability.probability, elementProbability);
				faultCombinationsAboveThreshold.add(fsp);
				pq.add(fsp);
			}
		}
		
		// So...now we have a priority queue with remaining fault combinations to be checked
		// for addition.  The PQ preserves the invariant that highest probability elements are 
		// first.  We attempt to combine with remainder (also in priority order). 
		// If unable to combine because combination below threshold, remove rest of 
		// elementProbability list (the rest will be below threshold for all subsequent elements).
		// Complete when either the PQ or the element list is empty.
		
		while (!pq.isEmpty() && !remainder.isEmpty()) {
			FaultSetProbability fsp = pq.remove();
			for (int i=0; i < remainder.size(); i++) {
				FaultProbability fp = remainder.get(i);
				double setProbability = fp.probability * fsp.probability; 
				if (setProbability < minProbability) {
					remainder.subList(i,  remainder.size()).clear();
				} else if (!fsp.elements.contains(fp)){
					FaultSetProbability newSet = 
						new FaultSetProbability(setProbability, fsp, fp);
					pq.add(newSet);
					faultCombinationsAboveThreshold.add(newSet);
				}
			}
		}
		System.out.println("Fault hypothesis: " + faultCombinationsAboveThreshold); 

		
		// Now faultCombinationsAboveThreshold contains all the valid fault combinations and
		// noFaultExpr has the default (no-fault) case.  Let's construct a proposition.
		Set<FaultProbability> elementProbabilitySet = new HashSet<>(elementProbabilities);
		Expr faultHypothesis = getNoFaultProposition(elementProbabilitySet);
		for (FaultSetProbability fsp: faultCombinationsAboveThreshold) {
			Set<FaultProbability> goodElements = new HashSet<>(elementProbabilities);
			goodElements.removeAll(fsp.elements);
			Expr local = getNoFaultProposition(goodElements);
			faultHypothesis = new BinaryExpr(local, BinaryOp.OR, faultHypothesis);
		}

		System.out.println("Probabilistic Fault Hypothesis is: " + faultHypothesis.toString());
		
		// Add this fault hypothesis as an assertion.
		builder.addAssertion(new AgreeStatement("", faultHypothesis, topNode.reference));
	}

	public void addTopLevelFaultOccurrenceConstraints(
			AnalysisBehavior ab, 
			AgreeNode topNode,
			AgreeNodeBuilder builder) {
		
		if (ab instanceof FaultCountBehavior) {
			addTopLevelMaxFaultOccurrenceConstraint(
					Integer.parseInt(((FaultCountBehavior)ab).getMaxFaults()), 
					topNode, builder);
		} else if (ab instanceof ProbabilityBehavior) {
			addTopLevelMaxFaultOccurrenceConstraint(
					Double.parseDouble(((ProbabilityBehavior)ab).getProbabilty()), 
					topNode, builder);
		}
	}
	
	/*
	 * Public accessor for the Map<Fault, String: LustreName> 
	 * This map is created in mapFaultToActiveNodeInterface (line 410).
	 */
	public Map<Fault, List<String>> getFaultToLustreNameMap(){
		return mapFaultToLustreNames;
	}
	
	/*
	 * Public accessor for the Map<Fault,String:faultyOutputName>
	 * This map is created in gatherFaultyOutputs (line 270).
	 */
	public Map<Fault, String> getMapFaultToPath(){
		return mapFaultToPath;
	}
	
	public class Pair<Expr, Fault>{
		private Expr ex;
		private Fault f;
		
		public Pair(Expr ex, Fault f) {
			this.ex = ex;
			this.f = f;
		}
	}

}
