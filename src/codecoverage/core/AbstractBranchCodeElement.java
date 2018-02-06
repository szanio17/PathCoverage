package codecoverage.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import codecoverage.coverage.CoveragePlugin;
import codecoverage.elements.BlockCodeElement;
import codecoverage.elements.SimpleCodeElement;
import codecoverage.view.Method;

public abstract class AbstractBranchCodeElement implements ICodeElement {
	protected int startLine;
	protected int endLine;
	protected ASTNode thisNode;
	protected Map<Integer, ICodeElement> codeElements;
	protected ICodeElement blockCode;
	protected List<Long> timestamps;
	protected int nodeType;
	protected boolean[] timestampsUsed;
	protected String label;

	public AbstractBranchCodeElement() {
		codeElements = new HashMap<Integer, ICodeElement>();
	}

	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {

		AnalyzeReturn ar = new AnalyzeReturn();
		long firstUnusedTS = CoveragePlugin.getFirstUnusedTS(timestamps, timestampsUsed);
		if (firstUnusedTS == -1) {
			ar.setReturnType(AnalyzeReturn.NO_SUCH_TIMESTAMP);
			return ar;
		}

		int code = AnalyzeReturn.OK;
		int counter = 0;
		CoveragePlugin.saveLCIToMethodCoverage(method, startLine, startLine, nodeType, false, true, false);
		while (code == AnalyzeReturn.OK) {
			ICodeElement nextLowerElement = nextElement;
			long firstCodeTS = blockCode.getFirstUnusedTimestamp();
			long secondCodeTS = nextElement.getFirstUnusedTimestamp();
			long firstCodeSecondTS = blockCode.getSecondUnusedTimestamp();
			if (firstCodeSecondTS < secondCodeTS && (firstCodeSecondTS != -1)) {
				SimpleCodeElement el = new SimpleCodeElement();
				List<Long> l = new ArrayList<Long>();
				l.add(firstCodeSecondTS);
				el.setTimestamps(l);
				nextLowerElement = el;
			}
			if (firstCodeTS == -1 || secondCodeTS == -1) {
				if (firstCodeTS == -1 && secondCodeTS == -1) {
					ar.setReturnType(AnalyzeReturn.THROWS);
					return ar;
				} else {
					if (firstCodeTS != -1) {
						if (counter > 0) {
							CoveragePlugin.saveLCIToMethodCoverage(method, startLine, startLine, nodeType, false, true,
									true);
						}
						counter++;
						AnalyzeReturn ar2 = blockCode.analyze(method, nextLowerElement, nextIterationTS);
						int returnType = ar2.getReturnType();
						if (returnType == AnalyzeReturn.RETURN) {
							CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
							return ar2;
						}
						if (returnType == AnalyzeReturn.BREAK) {
							if (ar2.getBreakLabel() == null) {
								if (counter > 0) {
									CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
								}
								code = AnalyzeReturn.OK;
							} else {
								if (ar2.getBreakLabel().equals(label)) {
									if (counter > 0) {
										CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
									}
									code = AnalyzeReturn.OK;
								} else {
									return ar2;
								}
							}
						}
						if (returnType == AnalyzeReturn.THROW || returnType == AnalyzeReturn.THROWS) {
							return ar2;
						}
						if (returnType == AnalyzeReturn.NO_SUCH_TIMESTAMP
								|| returnType == AnalyzeReturn.NOT_THIS_BLOCK) {
							if (counter > 0) {
								CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
							}
							return ar2;
						}
					} else {
						CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
						if (counter > 0) {
							CoveragePlugin.saveLCIToMethodCoverage(method, startLine, startLine, nodeType, false, true,
									true);
						}
						counter++;
						ar.setReturnType(AnalyzeReturn.OK);
						return ar;
					}
				}
			} else {
				if (firstCodeTS == secondCodeTS) {
					blockCode.searchAndTagReduntTS(firstCodeTS);
					CoveragePlugin.saveLCIToMethodCoverage(method, startLine, startLine, nodeType, false, true, true);
					ar.setReturnType(AnalyzeReturn.OK);
					return ar;
				}
				if (firstCodeTS <= secondCodeTS) {
					if (counter > 0) {
						CoveragePlugin.saveLCIToMethodCoverage(method, startLine, startLine, nodeType, false, true,
								true);
					}
					counter++;
					AnalyzeReturn ar2 = blockCode.analyze(method, nextLowerElement, nextIterationTS);
					int returnType = ar2.getReturnType();
					if (returnType == AnalyzeReturn.RETURN) {
						CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
						return ar2;
					}
					if (returnType == AnalyzeReturn.BREAK) {
						if (ar2.getBreakLabel() == null) {
							if (counter > 0) {
								CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
							}
							code = AnalyzeReturn.OK;
						} else {
							if (ar2.getBreakLabel().equals(label)) {
								if (counter > 0) {
									CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
								}
								code = AnalyzeReturn.OK;
							} else {
								return ar2;
							}
						}
					}
					if (returnType == AnalyzeReturn.THROW || returnType == AnalyzeReturn.THROWS) {
						return ar2;
					}
					if (returnType == AnalyzeReturn.NO_SUCH_TIMESTAMP || returnType == AnalyzeReturn.NOT_THIS_BLOCK) {
						if (counter > 0) {
							CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
						}
						return ar2;
					}
				} else {
					CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
					if (counter > 0) {
						CoveragePlugin.saveLCIToMethodCoverage(method, startLine, startLine, nodeType, false, true,
								true);
					}
					ar.setReturnType(AnalyzeReturn.OK);
					return ar;
				}
			}
		}
		CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
		ar.setReturnType(AnalyzeReturn.OK);
		return ar;
	}

	@Override
	public int getCountBranches() {
		return blockCode.getCountBranches() + 1;
	}

	// @Override
	// public long getFirstUnusedTimestamp() {
	// return blockCode.getFirstUnusedTimestamp();
	// }
	//
	// @Override
	// public long getSecondUnusedTimestamp() {
	// return blockCode.getSecondUnusedTimestamp();
	// }

	@Override
	public long getFirstUnusedTimestamp() {
		return CoveragePlugin.getFirstUnusedTS(timestamps, timestampsUsed);
	}

	@Override
	public long getSecondUnusedTimestamp() {
		if (timestamps != null) {
			boolean wasFirst = false;
			for (int i = 0; i < timestamps.size(); i++) {
				if (!timestampsUsed[i]) {
					if (wasFirst) {
						return timestamps.get(i);
					}
					wasFirst = true;
				}
			}
		}
		return -1;

	}

	@Override
	public void searchAndTagReduntTS(long timestamp) {
		if (blockCode != null) {
			blockCode.searchAndTagReduntTS(timestamp);
		}
	}

	public boolean addElement(ICodeElement element) {
		if (element instanceof BlockCodeElement) {
			if (blockCode == null) {
				BlockCodeElement el = (BlockCodeElement) element;
				el.setBranchBlock(true);
				blockCode = element;
				return true;
			} else {
				return false;
			}
		} else {
			if (blockCode == null) {
				if (element.getStartLine() != startLine) {
					blockCode = element;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void analyze(int deep) {
		if (blockCode != null) {
			blockCode.analyze(deep);
		}
	}

	@Override
	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	@Override
	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	@Override
	public int getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}

	public ASTNode getThisNode() {
		return thisNode;
	}

	public void setThisNode(ASTNode thisNode) {
		this.thisNode = thisNode;
	}

	public Map<Integer, ICodeElement> getCodeElements() {
		return codeElements;
	}

	public void setCodeElements(Map<Integer, ICodeElement> codeElements) {
		this.codeElements = codeElements;
	}

	@Override
	public List<Long> getTimestamps() {
		return timestamps;
	}

	@Override
	public void setTimestamps(List<Long> timestamps) {
		this.timestamps = timestamps;
		if (timestamps != null) {
			timestampsUsed = new boolean[timestamps.size()];
			Arrays.fill(timestampsUsed, false);
		}
	}

	public boolean[] getTimestampsUsed() {
		return timestampsUsed;
	}

	public void setTimestampsUsed(boolean[] timestampsUsed) {
		this.timestampsUsed = timestampsUsed;
	}

	public static ICodeElement getNextCode(Map<Integer, ICodeElement> map, int currentLine, int endLine) {
		for (int i = currentLine + 1; i <= endLine; i++) {
			ICodeElement el = map.get(new Integer(i));
			if (el != null) {
				return el;
			}
		}
		return null;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
