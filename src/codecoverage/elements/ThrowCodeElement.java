package codecoverage.elements;

import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.view.Method;

public class ThrowCodeElement extends SpecialSimpleCodeElement {

	public ThrowCodeElement() {
		codeType = AnalyzeReturn.THROW;
	}

	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {
		AnalyzeReturn ar = new AnalyzeReturn();
		CoveragePlugin.saveLCIToMethodCoverage(method, startLine * 1, endLine, nodeType, false);
		long firstUnusedTS = CoveragePlugin.getFirstUnusedTS(timestamps, timestampsUsed);
		if (firstUnusedTS == -1) {
			ar.setReturnType(AnalyzeReturn.NO_SUCH_TIMESTAMP);
			return ar;
		}
		CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);

		ar.setReturnType(codeType);
		ar.setThrowTS(firstUnusedTS);
		return ar;
	}
}
