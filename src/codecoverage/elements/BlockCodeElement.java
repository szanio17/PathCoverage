package codecoverage.elements;

import codecoverage.core.AbstractBranchCodeElement;
import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.view.Method;

public class BlockCodeElement extends AbstractBranchCodeElement{

	private boolean branchBlock = false;
	private boolean blockWithEndTimestamp = false;
	
	@Override
	public int getCountBranches() {
		int countBranches = 1;
		for(ICodeElement el : codeElements.values()) {
			countBranches *= el.getCountBranches();
		}
		return countBranches;
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
					if(returnType != AnalyzeReturn.OK) {
						return ar;
					}
					
				}
				else {
					AnalyzeReturn ar = el.analyze(method, nextElement, nextIterationTS);
					if(ar != null) {
						int returnType = ar.getReturnType();
						if(returnType != AnalyzeReturn.OK) {
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
				return el.getSecondUnusedTimestamp();
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
					System.out.println(tab + el.getClass().getName() + ", start:" + el.getStartLine() + ", end:" + el.getEndLine() + ", times:" + el.getTimestamps());
					AbstractBranchCodeElement element = (AbstractBranchCodeElement) el;
					element.analyze(deep+1);
				}
				else {
					el.analyze(deep);
					//System.out.println(tab + "Simple code, start:" + el.getStartLine() + ", end:" + el.getEndLine());
				}
			}
		}
	}
	
	public boolean addElement(ICodeElement element) {
		if(element instanceof BlockCodeElement) {
			if(!codeElements.containsKey(new Integer(element.getStartLine()))) {
				codeElements.put(new Integer(element.getStartLine()), element);
				return true;
			}
		}
		else {
			if(!codeElements.containsKey(new Integer(element.getStartLine()))) {
				codeElements.put(new Integer(element.getStartLine()), element);
				return true;
			}
		}
		return false;
	}

	public boolean isBranchBlock() {
		return branchBlock;
	}

	public void setBranchBlock(boolean branchBlock) {
		this.branchBlock = branchBlock;
	}

	public boolean isBlockWithEndTimestamp() {
		return blockWithEndTimestamp;
	}

	public void setBlockWithEndTimestamp(boolean blockWithEndTimestamp) {
		this.blockWithEndTimestamp = blockWithEndTimestamp;
	}

}
