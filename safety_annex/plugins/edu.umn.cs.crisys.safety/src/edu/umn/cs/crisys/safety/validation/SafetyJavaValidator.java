/*
 * generated by Xtext
 */
package edu.umn.cs.crisys.safety.validation;

import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.AnnexLibrary;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.NamedElement;

import com.rockwellcollins.atc.agree.agree.Arg;
import com.rockwellcollins.atc.agree.agree.Expr;
import com.rockwellcollins.atc.agree.agree.NamedID;
import com.rockwellcollins.atc.agree.agree.NestedDotID;

import edu.umn.cs.crisys.safety.safety.DurationStatement;
import edu.umn.cs.crisys.safety.safety.EqValue;
import edu.umn.cs.crisys.safety.safety.FaultStatement;
import edu.umn.cs.crisys.safety.safety.InputStatement;
import edu.umn.cs.crisys.safety.safety.IntervalEq;
import edu.umn.cs.crisys.safety.safety.OutputStatement;
import edu.umn.cs.crisys.safety.safety.SafetyPackage;
import edu.umn.cs.crisys.safety.safety.SetEq;
import edu.umn.cs.crisys.safety.safety.TemporalConstraint;
import edu.umn.cs.crisys.safety.safety.TriggerCondition;
import edu.umn.cs.crisys.safety.safety.TriggerStatement;
import edu.umn.cs.crisys.safety.services.SafetyGrammarAccess;
import edu.umn.cs.crisys.safety.services.SafetyGrammarAccess.TemporalConstraintElements;

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class SafetyJavaValidator extends AbstractSafetyJavaValidator {
	
	// Change package to SafetyPackage instead of AgreePackage.
	protected boolean isResponsible(Map<Object, Object> context, EObject eObject) {
		return (eObject.eClass().getEPackage() == SafetyPackage.eINSTANCE) || eObject instanceof AadlPackage;
	}

	@Check
	public void checkFaultSpecStmt(FaultStatement specStmt){
		if(specStmt.getStr().isEmpty()) {
			warning(specStmt, "Fault description is optional, but should "
					+ "not be an empty string.");
		}
	}
	
	
	// Input Statements
	@Check
	public void checkInput(InputStatement inputStmt){
		//String inConn = inputStmt.getIn_conn();
		//String outConn = inputStmt.getOut_conn();
		
		//if(inConn==null){
			//error(inConn, "Input connection cannot be null");
			//error("Input connection cannot be null", SafetyPackage.Literals.INPUT_STATEMENT__IN_CONN);
		//}
		
	}
	
	// Output Statements
	// Need input here in order to make sure output names match
	@Check
	public void checkOutput(OutputStatement outputStmt, InputStatement inputStmt){
		String outConn = outputStmt.getOut_conn();
		Expr nominalConn = outputStmt.getNom_conn();
		String inFaultConn = inputStmt.getOut_conn();
		
		if(!(inFaultConn.equals(outConn))){
			error(outputStmt, "Input statement fault output ID must be "
					+ "equal to output statement fault ID");
		}
		
		// Check to see if nominalConn is a component connection in AADL
	}
	
	// Duration statements
	@Check
	public void checkDuration(DurationStatement durationStmt){
		
		
	}
	
	
	// Trigger Statements
	@Check
	public void checkTriggerStatement(TriggerStatement triggerStmt){
		
		// First check the trigger condition
		checkTriggerCondition(triggerStmt.getCond());
		
		// Check the optional probability expression
		if(triggerStmt.getProbability() != null ){
			
			// Try casting string to double, catch exceptions to print out error
			double result = 0;
			try{
				result = Double.parseDouble(triggerStmt.getProbability());
			} catch(NullPointerException npe){
				error(triggerStmt, "Valid real number required");
			} catch(NumberFormatException nfe){
				error(triggerStmt, "Valid real number required");
			}
			
			// Now check to make sure it's a valid probability (btwn 0 and 1 inclusive)
			if((result < 0) || (result > 1)){
				error(triggerStmt, "Probability must be between 0 and 1 inclusive");
			}
			
			
		}
	}
	
	// This will need to be changed in order to deal with an
	// expression list. Not sure how to do that right now... 
	@Check
	public void checkTriggerCondition(TriggerCondition tc){
		if(tc != null){
			
			// Make sure expression list for trigger conditions is nonempty
			EList<Expr> list = tc.getExprList();
			if(list.isEmpty()) {
				error(tc, "Trigger condition list cannot be empty.");
			}
		}
	}
	
	
	// Check EqStatements: 
	@Check
	public void checkEqStatement(EqValue eqStmt){
		
		// Check to make sure we are within a fault statement
		AnnexLibrary library = EcoreUtil2.getContainerOfType(eqStmt, AnnexLibrary.class);
		if (library != null) {
			error(eqStmt, "Equation statments are only allowed in fault statements.");
		}
		
		
	}
	
	// Check the time interval passed in using Agree's method
	@Check
	public void checkIntervalEqStatement(IntervalEq intervalEq){
//		checkTimeInterval(intervalEq.getInterv());
	}
	
	// Check the set eq statements
	// Most of this is making sure integers are valid in set. 
	@Check
	public void checkSetEqStatement(SetEq setEq){
		
		if(setEq.getList().isEmpty()){
			error(setEq, "Set cannot be empty.");
		}
		
		// Try casting string to integer, catch exceptions to print out error
		Integer result = 0;
		try{
			result = Integer.parseInt(setEq.getL1());
		} catch(NullPointerException npe){
			error(setEq, "Valid integer required in set");
		} catch(NumberFormatException nfe){
			error(setEq, "Valid integer required in set");
		}
		
		// Now iterate through list making sure all integers are valid
		for(String item: setEq.getList()){
			try{
				result = Integer.parseInt(item);
			} catch(NullPointerException npe){
				error(setEq, "Valid integer required in set");
			} catch(NumberFormatException nfe){
				error(setEq, "Valid integer required in set");
			}
		}


		
		
	}
	
	
	

}
