package edu.umn.cs.crisys.safety.analysis.soteria.faultTree;

import edu.umn.cs.crisys.safety.analysis.ast.visitors.SoteriaFTAstVisitor;

public class SoteriaFTOrNode extends SoteriaFTNonLeafNode {
	public final String nodeOpStr = "SUM";

	public SoteriaFTOrNode(String propertyName, String propertyDescription) {
		super(propertyName, propertyDescription);
	}

	@Override
	public <T> T accept(SoteriaFTAstVisitor<T> visitor) {
		return visitor.visit(this);
	}

}
