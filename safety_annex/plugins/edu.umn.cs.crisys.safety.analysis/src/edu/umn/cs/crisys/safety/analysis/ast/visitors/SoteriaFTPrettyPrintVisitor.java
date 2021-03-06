package edu.umn.cs.crisys.safety.analysis.ast.visitors;

import edu.umn.cs.crisys.safety.analysis.SafetyException;
import edu.umn.cs.crisys.safety.analysis.soteria.faultTree.SoteriaFTAndNode;
import edu.umn.cs.crisys.safety.analysis.soteria.faultTree.SoteriaFTLeafNode;
import edu.umn.cs.crisys.safety.analysis.soteria.faultTree.SoteriaFTNode;
import edu.umn.cs.crisys.safety.analysis.soteria.faultTree.SoteriaFTNonLeafNode;
import edu.umn.cs.crisys.safety.analysis.soteria.faultTree.SoteriaFTOrNode;
import edu.umn.cs.crisys.safety.analysis.soteria.faultTree.SoteriaFaultTree;

public class SoteriaFTPrettyPrintVisitor implements SoteriaFTAstVisitor<Void> {
	private StringBuilder sb = new StringBuilder();

	@Override
	public String toString() {
		return sb.toString();
	}

	protected void write(Object o) {
		sb.append(o);
	}

	protected void writeln(Object o) {
		sb.append(o);
		newline();
	}

	private static final String seperator = System.getProperty("line.separator");

	private void newline() {
		write(seperator);
	}

	@Override
	public Void visit(SoteriaFaultTree ft) {
		write(ft.includeStr);
		newline();
		// print leaf nodes
		for (SoteriaFTLeafNode leaf : ft.leafNodes.values()) {
			leaf.accept(this);
		}
		// print non leaf nodes
		for (SoteriaFTNonLeafNode nonLeaf : ft.sortedIntermediateNodes) {
			nonLeaf.accept(this);
		}
		//compute cutsets and probabilities for each root node
		for (SoteriaFTNonLeafNode root : ft.rootNodes.values()) {
			String rootName = root.propertyName;
			writeln("(* ----- CUTSET WITH PROBABILITIES ----- *)");
			writeln("cutsets "+rootName+";;");
			writeln("probErrorCut " + rootName + ";;");
			writeln("probErrorCutImp " + rootName + ";;");
			writeln("(* ----- FAULT TREE VISUALIZATIONS ----- *)");
			// fault tree visualization
			write("dot_gen_show_direct_tree_file ~rend:\"pdf\" ");
			writeln("\"" + rootName + "_direct_ftree.gv\" " + rootName + " ;;");
			write("dot_gen_show_tree_file ~rend:\"pdf\" ");
			writeln("\"" + rootName + "_optimized_ftree.gv\" " + rootName + " ;;");
			newline();
		}

	return null;

	}

	@Override
	public Void visit(SoteriaFTLeafNode leaf) {
		writeln("let " + leaf.soteriaFaultName + " = ");
		writeln("Leaf");
		write("    ((\"" + leaf.compName + "\",");
		write("\"" + leaf.soteriaFaultName + "\"),");
		write(leaf.failureRate + ", ");
		writeln(leaf.exposureTime + ");;");
		return null;
	}

	@Override
	public Void visit(SoteriaFTNonLeafNode nonLeaf) {
		if (nonLeaf instanceof SoteriaFTOrNode) {
			SoteriaFTOrNode orNode = (SoteriaFTOrNode) nonLeaf;
			orNode.accept(this);
		} else if (nonLeaf instanceof SoteriaFTAndNode) {
			SoteriaFTAndNode andNode = (SoteriaFTAndNode) nonLeaf;
			andNode.accept(this);
		} else {
			throw new SafetyException("Not instanstiated non leaf node " + nonLeaf.nodeName);
		}
		return null;
	}

	@Override
	public Void visit(SoteriaFTOrNode orNode) {
		printNonLeafNode(orNode.nodeOpStr, orNode);
		return null;
	}

	@Override
	public Void visit(SoteriaFTAndNode andNode) {
		printNonLeafNode(andNode.nodeOpStr, andNode);
		return null;
	}

	private void printNonLeafNode(String nodeOpStr, SoteriaFTNonLeafNode nonLeaf) {
		writeln("let " + nonLeaf.propertyName + " = ");
		writeln(nodeOpStr + " [");
		boolean multipleElem = false;
		for (SoteriaFTNode node : nonLeaf.childNodes.values()) {
			if (multipleElem) {
				writeln(";");
			}
			write(node.nodeName);
			multipleElem = true;
		}
		writeln("    ];;");
	}

}
