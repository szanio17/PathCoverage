package codecoverage.elements;

import codecoverage.core.AnalyzeReturn;

public class ReturnCodeElement extends SpecialSimpleCodeElement {

	
	public ReturnCodeElement() {
		codeType = AnalyzeReturn.RETURN;
	}

}
