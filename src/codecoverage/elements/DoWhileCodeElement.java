package codecoverage.elements;

import codecoverage.core.AbstractBranchCodeElement;
import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.view.Method;

public class DoWhileCodeElement extends AbstractBranchCodeElement {

	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {
		// throw new UnsupportedOperationException("'Do-while' is not suported,
		// yet");
		int code = AnalyzeReturn.OK;
		AnalyzeReturn ar = new AnalyzeReturn();
		long firstDoWhileTS = blockCode.getFirstUnusedTimestamp();
		int counter = 0;
		if (firstDoWhileTS == -1) {
			ar.setReturnType(AnalyzeReturn.OK);
			return ar;
		}
		while (code == AnalyzeReturn.OK) {
			AnalyzeReturn ar2 = blockCode.analyze(method, nextElement, nextIterationTS);
			int returnType = ar2.getReturnType();
			if (returnType == AnalyzeReturn.RETURN) {
				return ar2;
			}
			if (returnType == AnalyzeReturn.BREAK) {
				if (ar2.getBreakLabel() == null) {
					code = AnalyzeReturn.NOT_THIS_BLOCK;
				} else {
					if (ar2.getBreakLabel().equals(label)) {
						code = AnalyzeReturn.NOT_THIS_BLOCK;
					} else {
						return ar2;
					}
				}
			}
			if (returnType == AnalyzeReturn.THROW || returnType == AnalyzeReturn.THROWS) {
				return ar2;
			}
			if (code == AnalyzeReturn.OK) {
				long whileFirstTS = blockCode.getFirstUnusedTimestamp();
				long nextFirstTS = nextElement.getFirstUnusedTimestamp();
				if (whileFirstTS != -1) {
					if ((nextFirstTS < whileFirstTS && nextFirstTS != -1)) {
						code = AnalyzeReturn.NOT_THIS_BLOCK;
					}
				} else {
					code = AnalyzeReturn.NOT_THIS_BLOCK;
				}
			}
			if (counter > 0) {
				CoveragePlugin.saveLCIToMethodCoverage(method, endLine, endLine, nodeType, false, true, true);
			} else {
				CoveragePlugin.saveLCIToMethodCoverage(method, endLine, endLine, nodeType, false, true, false);
			}

		}
		ar.setReturnType(AnalyzeReturn.OK);
		return ar;
	}

	@Override
	public long getFirstUnusedTimestamp() {
		return blockCode.getFirstUnusedTimestamp();
	}

	@Override
	public long getSecondUnusedTimestamp() {
		return blockCode.getSecondUnusedTimestamp();
	}

	@Override
	public void searchAndTagReduntTS(long timestamp) {
		blockCode.searchAndTagReduntTS(timestamp);
	}
}
