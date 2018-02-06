package codecoverage.elements;

import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.view.Method;

public class BreakCodeElement extends SpecialSimpleCodeElement {

	private String label;

	public BreakCodeElement(String label) {
		codeType = AnalyzeReturn.BREAK;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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
		ar.setBreakLabel(label);
		return ar;
	}
}
