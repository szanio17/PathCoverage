package codecoverage.elements;

import codecoverage.core.AbstractBranchCodeElement;
import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.view.Method;

public class TryCodeElement extends AbstractBranchCodeElement {

	private ICodeElement finallyBlock;

	@Override
	public int getCountBranches() {
		return blockCode.getCountBranches();
	}

	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {
		AnalyzeReturn ar = blockCode.analyze(method, nextElement, nextIterationTS);
		int returnType = ar.getReturnType();
		if (returnType == AnalyzeReturn.OK) {
			return ar;
		} else {
			if (returnType == AnalyzeReturn.THROW || returnType == AnalyzeReturn.THROWS) {
				AbstractBranchCodeElement code = getCatchBlock(ar.getThrowTS(), nextIterationTS);
				boolean catchBlockThrow = false;
				boolean finallyBlockThrow = false;
				AnalyzeReturn arC = null;
				if (code != null) {
					arC = code.analyze(method, nextElement, nextIterationTS);
					int returnTypeC = arC.getReturnType();
					if (returnTypeC == AnalyzeReturn.THROW || returnTypeC == AnalyzeReturn.THROWS) {
						catchBlockThrow = true;
					}
				}
				AnalyzeReturn arF = null;
				if (finallyBlock != null) {
					arF = finallyBlock.analyze(method, nextElement, nextIterationTS);
					int returnTypeC = arF.getReturnType();
					if (returnTypeC == AnalyzeReturn.THROW || returnTypeC == AnalyzeReturn.THROWS) {
						finallyBlockThrow = true;
					}
				}
				if (finallyBlockThrow) {
					return arF;
				} else if (catchBlockThrow) {
					return arC;
				}
				if (code != null) {
					return arC;
				}
				return ar;
			} else if (returnType == AnalyzeReturn.BREAK) {
				if (ar.getBreakLabel() != null) {
					if (ar.getBreakLabel().equals(label)) {
						CoveragePlugin.saveLCIToMethodCoverage(method, startLine, endLine, nodeType, false);
						ar.setBreakLabel(null);
						ar.setReturnType(AnalyzeReturn.OK);
					}
				}
			}
		}
		return ar;
	}

	private AbstractBranchCodeElement getCatchBlock(long throwTS, long nextIterationTS) {
		ICodeElement catchBlock = null;

		for (ICodeElement block : codeElements.values()) {
			long catchFirstTS = block.getFirstUnusedTimestamp();
			if (catchFirstTS < nextIterationTS || nextIterationTS == -1) {
				if (catchBlock == null) {
					catchBlock = block;
				} else {
					long blockTS = catchBlock.getFirstUnusedTimestamp();
					if (catchFirstTS < blockTS) {
						catchBlock = block;
					}
				}
			}
		}
		return (AbstractBranchCodeElement) catchBlock;
	}

	@Override
	public boolean addElement(ICodeElement element) {
		if (element instanceof BlockCodeElement) {
			if (blockCode == null) {
				BlockCodeElement el = (BlockCodeElement) element;
				el.setBranchBlock(true);
				blockCode = el;
				return true;
			} else {
				BlockCodeElement el = (BlockCodeElement) element;
				el.setBranchBlock(true);
				finallyBlock = el;
				return true;
			}

		} else if (element instanceof CatchCodeElement) {
			codeElements.put(element.getStartLine(), element);
			return true;
		}
		return false;
	}

	@Override
	public void analyze(int deep) {
		String tab = "";
		for (int i = 0; i < deep; i++) {
			tab += "	";
		}
		if (blockCode != null) {
			blockCode.analyze(deep + 1);
		}
		for (ICodeElement el : codeElements.values()) {
			if (el instanceof CatchCodeElement) {
				System.out.println(tab + "codecoverage.elements.CatchCodeElement, start:" + el.getStartLine() + ",end:"
						+ el.getEndLine());
				CatchCodeElement catchCode = (CatchCodeElement) el;
				catchCode.analyze(deep + 1);
			}
		}
		if (finallyBlock != null) {
			System.out.println(
					tab + "finally, start:" + finallyBlock.getStartLine() + ",end:" + finallyBlock.getEndLine());
			finallyBlock.analyze(deep + 1);
		}
	}

}
