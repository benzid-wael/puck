package puck.parser.gen;

import java.util.List;

import puck.parser.RuleStructure;

public class GreedySegmentationGenRuleMultiply<C, L>  extends SimpleGenRuleMultiply<C, L> {

	public static final int BINARY_NUM_SEGMENTS = 24;
	
	public GreedySegmentationGenRuleMultiply(RuleStructure<C, L> structure) {
		super(structure);
	}

	public List<IndexedUnaryRule<C, L>>[] segmentUnaries(List<IndexedUnaryRule<C, L>> indexedUnaryRules) {
		List<IndexedUnaryRule<C, L>>[] segmentation = new List[] {indexedUnaryRules};
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (List segment : segmentation) {
			min = Math.min(segment.size(), min);
			max = Math.max(segment.size(), max);
		}
		System.out.println("min unary segment size: "+min);
		System.out.println("max unary segment size: "+max);
		return segmentation;
	}

	public List<IndexedBinaryRule<C, L>>[][] segmentBinaries(List<IndexedBinaryRule<C, L>> indexedBinaryRules) {
		return null;
	}

}