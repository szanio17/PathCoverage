package codecoverage.elements;

import codecoverage.core.AbstractBranchCodeElement;
import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.view.Method;

public class IfCodeElement extends AbstractBranchCodeElement {

	private ICodeElement thenBlock;
	private ICodeElement elseBlock;

	@Override
	public boolean addElement(ICodeElement element) {
		if (element instanceof BlockCodeElement) {
			if (thenBlock == null) {
				thenBlock = element;
			} else {
				elseBlock = element;
			}
			return true;
		} else {
			if (thenBlock == null) {
				if (element.getStartLine() != startLine) {
					thenBlock = element;
				}
			} else {
				elseBlock = element;
			}
		}
		return false;
	}

	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {
		AnalyzeReturn ar = new AnalyzeReturn();
		long firstUnusedTS = CoveragePlugin.getFirstUnusedTS(timestamps, timestampsUsed);
		if (firstUnusedTS == -1) {
			ar.setReturnType(AnalyzeReturn.NO_SUCH_TIMESTAMP);
			return ar;
		}
		CoveragePlugin.saveLCIToMethodCoverage(method, startLine, startLine, nodeType, false);
		if (elseBlock == null) {
			long firstThen = thenBlock.getFirstUnusedTimestamp();
			long firstNext = nextElement.getFirstUnusedTimestamp();
			if ((firstThen < firstNext && firstThen != -1) || firstNext == -1) {
				// method.getPathCoverage().add(startLine);
				// coverageData.add((short)1);
				ar = thenBlock.analyze(method, nextElement, nextIterationTS);
				int returnType = ar.getReturnType();
				if (returnType == AnalyzeReturn.BREAK) {
					if (ar.getBreakLabel() != null) {
						if (ar.getBreakLabel().equals(label)) {
							CoveragePlugin.saveLCIToMethodCoverage(method, startLine, endLine, nodeType, false);
							ar.setBreakLabel(null);
							ar.setReturnType(AnalyzeReturn.OK);
						}
					}
				}
				CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
				// coverageData.add((short)5);
				return ar;
			} else {
				CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
				ar.setReturnType(AnalyzeReturn.OK);
				return ar;
			}
		} else {
			long firstThen = thenBlock.getFirstUnusedTimestamp();
			long firstElse = elseBlock.getFirstUnusedTimestamp();
			if (firstThen == -1 || firstElse == -1) {
				if (firstThen == -1 && firstElse == -1) {
					ar.setReturnType(AnalyzeReturn.THROWS);
					return ar;
				} else {
					if (firstThen != -1) {
						// coverageData.add((short)1);
						// method.getPathCoverage().add(startLine);
						ar = thenBlock.analyze(method, nextElement, nextIterationTS);
						int returnType = ar.getReturnType();
						if (returnType == AnalyzeReturn.BREAK) {
							if (ar.getBreakLabel().equals(label)) {
								ar.setBreakLabel(null);
								ar.setReturnType(AnalyzeReturn.OK);
							}
						}
						CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
						// coverageData.add((short)5);
						return ar;
					} else {
						// coverageData.add((short)2);
						// method.getPathCoverage().add(elseBlock.getStartLine());
						ar = elseBlock.analyze(method, nextElement, nextIterationTS);
						int returnType = ar.getReturnType();
						if (returnType == AnalyzeReturn.BREAK) {
							if (ar.getBreakLabel().equals(label)) {
								ar.setBreakLabel(null);
								ar.setReturnType(AnalyzeReturn.OK);
							}
						}
						CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
						// coverageData.add((short)5);
						return ar;
					}
				}
			} else {
				if (firstThen < firstElse) {
					// coverageData.add((short)1);
					// method.getPathCoverage().add(startLine);
					ar = thenBlock.analyze(method, nextElement, nextIterationTS);
					int returnType = ar.getReturnType();
					if (returnType == AnalyzeReturn.BREAK) {
						if (ar.getBreakLabel().equals(label)) {
							ar.setBreakLabel(null);
							ar.setReturnType(AnalyzeReturn.OK);
						}
					}
					CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
					// coverageData.add((short)5);
					return ar;
				} else {
					// coverageData.add((short)2);
					// method.getPathCoverage().add(elseBlock.getStartLine());
					ar = elseBlock.analyze(method, nextElement, nextIterationTS);
					int returnType = ar.getReturnType();
					if (returnType == AnalyzeReturn.BREAK) {
						if (ar.getBreakLabel().equals(label)) {
							ar.setBreakLabel(null);
							ar.setReturnType(AnalyzeReturn.OK);
						}
					}
					CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
					// coverageData.add((short)5);
					return ar;
				}
			}

		}
	}

	// @Override
	// public long getFirstUnusedTimestamp() {
	// if(elseBlock == null) {
	// return thenBlock.getFirstUnusedTimestamp();
	// }
	// else {
	// long thenTimestamp = thenBlock.getFirstUnusedTimestamp();
	// long elseTimestamp = elseBlock.getFirstUnusedTimestamp();
	// if(thenTimestamp == -1 || elseTimestamp == -1) {
	// if(thenTimestamp == -1 && elseTimestamp == -1) {
	// return -1;
	// }
	// else {
	// if(thenTimestamp != -1) {
	// return thenTimestamp;
	// }
	// else {
	// return elseTimestamp;
	// }
	// }
	// }
	// else {
	// if(thenTimestamp < elseTimestamp) {
	// return thenTimestamp;
	// }
	// else {
	// return elseTimestamp;
	// }
	// }
	//
	// }
	// }
	//
	// @Override
	// public long getSecondUnusedTimestamp() {
	// if(elseBlock == null) {
	// return thenBlock.getSecondUnusedTimestamp();
	// }
	// else {
	// long thenTimestamp = thenBlock.getSecondUnusedTimestamp();
	// long elseTimestamp = elseBlock.getSecondUnusedTimestamp();
	// if(thenTimestamp == -1 || elseTimestamp == -1) {
	// if(thenTimestamp == -1 && elseTimestamp == -1) {
	// return -1;
	// }
	// else {
	// if(thenTimestamp != -1) {
	// return thenTimestamp;
	// }
	// else {
	// return elseTimestamp;
	// }
	// }
	// }
	// else {
	// if(thenTimestamp < elseTimestamp) {
	// return thenTimestamp;
	// }
	// else {
	// return elseTimestamp;
	// }
	// }
	// }
	// }

	@Override
	public void analyze(int deep) {
		String tab = "";
		for (int i = 0; i < deep - 1; i++) {
			tab += "	";
		}
		if (thenBlock != null) {
			thenBlock.analyze(deep);
			if (elseBlock != null) {
				System.out.println(tab + elseBlock.getClass().getName() + ", start:" + elseBlock.getStartLine()
						+ ", end:" + elseBlock.getEndLine());
				elseBlock.analyze(deep);
			}
		}
	}

	@Override
	public int getCountBranches() {
		int countBranches = thenBlock.getCountBranches();
		if (elseBlock != null) {
			countBranches += elseBlock.getCountBranches();
		} else {
			countBranches += 1;
		}
		return countBranches;
	}
}
