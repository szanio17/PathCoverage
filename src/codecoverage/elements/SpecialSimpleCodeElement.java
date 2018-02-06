package codecoverage.elements;

import codecoverage.core.AbstractSimpleCodeElement;
import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.view.Method;

public abstract class SpecialSimpleCodeElement extends AbstractSimpleCodeElement {

	protected int codeType;

	@Override
	public void analyze(int deep) {
		String tab = "";
		for (int i = 0; i < deep; i++) {
			tab += "	";
		}
		System.out.println(tab + codeType + " code, start:" + startLine + ", end:" + endLine + ",times:" + timestamps);
	}

	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {
		AnalyzeReturn ar = new AnalyzeReturn();
		CoveragePlugin.saveLCIToMethodCoverage(method, startLine, endLine, nodeType, false);
		long firstUnusedTS = CoveragePlugin.getFirstUnusedTS(timestamps, timestampsUsed);
		if (firstUnusedTS == -1) {
			ar.setReturnType(AnalyzeReturn.NO_SUCH_TIMESTAMP);
			return ar;
		}
		CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);

		ar.setReturnType(codeType);
		return ar;
	}
}
