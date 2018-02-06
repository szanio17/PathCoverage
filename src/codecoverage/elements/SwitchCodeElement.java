package codecoverage.elements;

import java.util.Set;

import codecoverage.core.AbstractBranchCodeElement;
import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.view.Method;

public class SwitchCodeElement extends AbstractBranchCodeElement {

	@Override
	public int getCountBranches() {
		return codeElements.size() + 1;
	}

	@Override
	public boolean addElement(ICodeElement element) {
		if (element instanceof SwitchCaseCodeElement) {
			codeElements.put(element.getStartLine(), element);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {
		AnalyzeReturn ar = new AnalyzeReturn();
		boolean found = false;
		for (int i = 0; i < timestamps.size(); i++) {
			if (!timestampsUsed[i]) {
				found = true;
				break;
			}
		}
		if (!found) {
			ar.setReturnType(AnalyzeReturn.NO_SUCH_TIMESTAMP);
			return ar;
		}
		CoveragePlugin.saveLCIToMethodCoverage(method, startLine, startLine, nodeType, false);

		if (codeElements.size() > 0) {
			Set<Integer> set = codeElements.keySet();
			int index = 0;
			for (Integer i : set) {
				index = i;
				break;
			}
			ICodeElement elCase = codeElements.get(index);
			if (elCase == null) {
				ar.setReturnType(AnalyzeReturn.NOT_THIS_BLOCK);
			}
			for (Integer i : set) {
				if (i == index) {
					continue;
				}
				ICodeElement nextCase = codeElements.get(i);
				long elFirstTS = elCase.getFirstUnusedTimestamp();
				if (elFirstTS == -1) {
					elCase = nextCase;
				} else {
					long nextElFirstTS = nextCase.getFirstUnusedTimestamp();
					if (nextElFirstTS != -1) {
						if (nextElFirstTS < elFirstTS) {
							elCase = nextCase;
						}
					}
				}
			}
			long elFirstTS = elCase.getFirstUnusedTimestamp();
			if (elFirstTS != -1) {
				long nextFirstTS = nextElement.getFirstUnusedTimestamp();
				if (elFirstTS < nextFirstTS) {
					for (int i = 0; i < timestamps.size(); i++) {
						if (!timestampsUsed[i]) {
							timestampsUsed[i] = true;
							break;
						}
					}
					ar = elCase.analyze(method, nextElement, nextIterationTS);
					return ar;
				} else {
					for (int i = 0; i < timestamps.size(); i++) {
						if (!timestampsUsed[i]) {
							timestampsUsed[i] = true;
							break;
						}
					}
					ar.setReturnType(AnalyzeReturn.NOT_THIS_BLOCK);
					return ar;
				}
			}
		}
		ar.setReturnType(AnalyzeReturn.OK);
		return ar;
	}

	@Override
	public void analyze(int deep) {
		String tab = "";
		for (int i = 0; i < deep; i++) {
			tab += "	";
		}
		for (int i = startLine; i <= endLine; i++) {
			if (codeElements.containsKey(new Integer(i))) {
				ICodeElement el = codeElements.get(new Integer(i));
				if (el instanceof SwitchCaseCodeElement) {
					System.out.println(tab + el.getClass().getName() + ", start:" + el.getStartLine() + ", end:"
							+ el.getEndLine());
					AbstractBranchCodeElement element = (AbstractBranchCodeElement) el;
					element.analyze(deep + 1);
				}
			}
		}
	}
}
