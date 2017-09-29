package edu.umn.cs.crisys.safety.analysis.transform;


import com.rockwellcollins.atc.agree.analysis.ast.AgreeProgram;
import com.rockwellcollins.atc.agree.analysis.ast.visitors.AgreeASTPrettyprinter;
import com.rockwellcollins.atc.agree.analysis.extentions.AgreeAutomater;
import edu.umn.cs.crisys.safety.analysis.ast.visitors.AddFaultsToNodeVisitor;

public class AddFaultsToAgree implements AgreeAutomater {

	private static boolean transformFlag = false;

	
	/* For each AgreeNode:
	 * ! 1. Find the set of "faulty" outputs. Ensure that only one fault occurs per output
	 * (test: print them & verify)
	 * 		We need to map "faulty" outputs as they are define in the error 
	 * 		annex to their names in AGREE. 
	 * 
	 * ! 2. For each of these outputs define a "nominal" value
	 * (test: print updated AST)
	 * ! 3. For each property involving a faulty output, replace the faulty output id with the
	 * nominal output id.
	 * (test: print updated AST)
	 * ! 4. For each fault, define a "fault_occurred" input (unique name?)
	 * ! 4a. Create eq with unique name for each local eq. in the fault
	 * (test: print updated AST)
	 * ! 5. For each fault, define an assignment equation that assigns unique "fault" names
	 * to the fault node outputs.
	 * (test: print updated AST)
	 * ! 6. For each of the "faulty" outputs assign it to the appropriate "fault" name node output.
	 * (test: print updated AST)
	 *
	 * For the top-level node:
	 * 	
	 * ! 1. For each subcomponent node
		For each subcomponent fault (depth-first)
			0. Perform a traversal to find all the node/fault pairs
			1a. Define an unconstrained local eq. to represent each fault-event 
			1b. Define a constrained local eq. to assign fault-active value depending on 
				fault duration in node.
			1c. Assign subcomponent fault input to fault-active eq with assertions (yay!) 
	            (test: print updated AST)
		! 2. Assign faults-active equation to sum of all fault-active values
			(test: print updated AST)
		3. Assert that this value is <= 1 (FOR NOW!)	
			(test: print updated AST)
		4. Use shiny new fault annex to perform safety analysis
			(test: analysis results)
	 


	 */

	/*
	 * transform:
	 * @param AgreeProgram: this is the agree program that comes in from the extension point.
	 * @return AgreeProgram: this is either the unmodified original program,
	 * or a transformed program (safety analysis)
	 *
	 */
	@Override
	public AgreeProgram transform(AgreeProgram program) {

		// check to make sure we are supposed to transform program
		if (!AddFaultsToAgree.getTransformFlag()) {
			return program;
		}

		AddFaultsToNodeVisitor faultVisitor = new AddFaultsToNodeVisitor();
		
		try{
			program = faultVisitor.visit(program);
			{ 
				AgreeASTPrettyprinter pp = new AgreeASTPrettyprinter();
				pp.visit(program);
				System.out.println(pp.toString());
			}
		}
		catch (Throwable t) {
			System.out.println("Something went wrong during safety analysis: " + t.toString());
		}
		finally {
			System.out.println("completed performing safety analysis transformation");
		}
		
		
		return program;
	}

	/*
	 * setTransformFlag:
	 * @param none
	 * @return none
	 * Sets the transform flag to true.
	 */
	public static void setTransformFlag(boolean flag) {

		transformFlag = flag;

	}

	/*
	 * getTransformFlag
	 * @param none
	 * @return boolean flag
	 * Returns the value of the flag.
	 */
	public static boolean getTransformFlag(){
		return transformFlag;
	}


}