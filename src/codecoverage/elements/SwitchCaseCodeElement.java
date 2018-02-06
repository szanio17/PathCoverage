package codecoverage.elements;

import java.util.Set;

import codecoverage.core.AbstractBranchCodeElement;
import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.view.Method;

public class SwitchCaseCodeElement extends AbstractBranchCodeElement {

	private boolean isLastCase = false;
	
	@Override
	public int getCountBranches() {
		return 0;
	}

	@Override
	public boolean addElement(ICodeElement element) {
		if(element instanceof BlockCodeElement) {
			codeElements.put(element.getStartLine(), element);
			return true;
		}
		else {
			if(element.getStartLine() != startLine) {
				codeElements.put(element.getStartLine(), element);
				endLine = element.getStartLine();
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {
		for(int i = startLine ; i <= endLine; i++) {
			if(codeElements.containsKey(new Integer(i))) {
				ICodeElement el = codeElements.get(new Integer(i));
				ICodeElement nextElement2 = AbstractBranchCodeElement.getNextCode(codeElements, el.getEndLine(), endLine);
				ICodeElement nextNextElement = AbstractBranchCodeElement.getNextCode(codeElements, el.getEndLine()+1, endLine);
				ICodeElement nextElementTmp;
				if(nextNextElement != null) {
					if(nextElement2 instanceof AbstractBranchCodeElement) {
						long nextTmp = nextElement2.getFirstUnusedTimestamp();
						if(nextTmp == -1) {
							nextElementTmp = nextNextElement;
						}
						else {
							nextElementTmp = nextElement2;
						}
					}
					else {
						nextElementTmp = nextElement2;
					}
				}
				else {
					nextElementTmp = nextElement2;
				}
				if(nextElement2 != null) {
					AnalyzeReturn ar = el.analyze(method, nextElementTmp, nextIterationTS);
					int returnType = ar.getReturnType();
					if(returnType == AnalyzeReturn.RETURN || returnType == AnalyzeReturn.THROW ||
							returnType == AnalyzeReturn.NO_SUCH_TIMESTAMP) {
						AnalyzeReturn ar2 = new AnalyzeReturn();
						ar2.setReturnType(AnalyzeReturn.RETURN);
						return ar2;
					}
					else if(returnType == AnalyzeReturn.THROWS) {
						AnalyzeReturn ar2 = new AnalyzeReturn();
						ar2.setReturnType(AnalyzeReturn.THROWS);
						return ar2;
					}
				}
				else {
					AnalyzeReturn ar = el.analyze(method, nextElement, nextIterationTS);
					if(ar != null) {
						int returnType = ar.getReturnType();
						if(returnType == AnalyzeReturn.RETURN || returnType == AnalyzeReturn.THROW ||
								returnType == AnalyzeReturn.NO_SUCH_TIMESTAMP) {
							AnalyzeReturn ar2 = new AnalyzeReturn();
							ar2.setReturnType(AnalyzeReturn.RETURN);
							return ar2;
						}
						else if(returnType == AnalyzeReturn.THROWS) {
							return ar;
						}
					}
					AnalyzeReturn ar2 = new AnalyzeReturn();
					ar2.setReturnType(AnalyzeReturn.OK);
					return ar2;
				}
			}
		}
		AnalyzeReturn ar2 = new AnalyzeReturn();
		ar2.setReturnType(AnalyzeReturn.NO_SUCH_TIMESTAMP);
		return ar2;
	}
	
	public void repairLastCase() {
		ICodeElement last = null;
		ICodeElement secondLast = null;
		if(codeElements.size() >= 2) {
			Set<Integer> set = codeElements.keySet();
			boolean secondLastB = false;
			for(Integer i: set) {
				if(!secondLastB) {
					secondLast = codeElements.get(i);
					secondLastB = true;
					continue;
				}
				last = codeElements.get(i);
				break;
			}
			int index = 0;
			for(Integer i : set) {
				if(index < 2) {
					index++;
					continue;
				}
				secondLast = last;
				last = codeElements.get(i);
			}
			last.setTimestamps(secondLast.getTimestamps());
		}
	}
	
	@Override
	public void searchAndTagReduntTS(long timestamp) {
		for(ICodeElement ce : codeElements.values()) {
			ce.searchAndTagReduntTS(timestamp);
		}
	}
	
	@Override
	public long getFirstUnusedTimestamp() {
		for(int i = startLine ; i <= endLine; i++) {
			if(codeElements.containsKey(new Integer(i))) {
				ICodeElement el = codeElements.get(new Integer(i));
				return el.getFirstUnusedTimestamp();
			}
		}
		return -1;
	}
	
	@Override
	public long getSecondUnusedTimestamp() {
		for(int i = startLine ; i <= endLine; i++) {
			if(codeElements.containsKey(new Integer(i))) {
				ICodeElement el = codeElements.get(new Integer(i));
				return el.getFirstUnusedTimestamp();
			}
		}
		return -1;
	}
	
	@Override
	public void analyze(int deep) {
		String tab = "";
		for(int i = 0; i < deep; i++) {
			tab += "	";
		}
		for(int i = startLine ; i <= endLine; i++) {
			if(codeElements.containsKey(new Integer(i))) {
				ICodeElement el = codeElements.get(new Integer(i));
				if(el instanceof AbstractBranchCodeElement) {
					System.out.println(tab + el.getClass().getName() + ", start:" + el.getStartLine() + ", end:" + el.getEndLine());
					AbstractBranchCodeElement element = (AbstractBranchCodeElement) el;
					element.analyze(deep+1);
				}
				else {
					System.out.println(tab + "Simple code, start:" + el.getStartLine() + ", end:" + el.getEndLine());
				}
			}
		}
	}

	public boolean isLastCase() {
		return isLastCase;
	}

	public void setLastCase(boolean isLastCase) {
		this.isLastCase = isLastCase;
	}
}
