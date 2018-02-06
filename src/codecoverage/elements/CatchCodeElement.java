package codecoverage.elements;

import codecoverage.core.AbstractBranchCodeElement;
import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.view.Method;

public class CatchCodeElement extends AbstractBranchCodeElement {

	@Override
	public int getCountBranches() {
		return 0;
	}

	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {
		CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
		CoveragePlugin.saveLCIToMethodCoverage(method, startLine, startLine, nodeType, false);
		AnalyzeReturn ar = blockCode.analyze(method, nextElement, nextIterationTS);
		return ar;
	}
}
