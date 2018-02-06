package codecoverage.core;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import codecoverage.coverage.CoveragePlugin;
import codecoverage.view.Method;

public abstract class AbstractSimpleCodeElement implements ICodeElement {
	protected int startLine;
	protected int endLine;
	protected int nodeType;
	protected ASTNode thisNode;
	protected List<Long> timestamps;
	protected boolean[] timestampsUsed;

	@Override
	public int getCountBranches() {
		return 1;
	}

	@Override
	public void analyze(int deep) {
		String tab = "";
		for (int i = 0; i < deep; i++) {
			tab += "	";
		}
		System.out.println(tab + "code, start:" + startLine + ", end:" + endLine + ",times:" + timestamps);
	}

	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {
		long nextTimestamp = -1;
		if (nextElement != null) {
			nextTimestamp = nextElement.getFirstUnusedTimestamp();
		}
		AnalyzeReturn ar = new AnalyzeReturn();
		if (timestamps != null) {
			if (timestamps.isEmpty()) {
				ar.setReturnType(AnalyzeReturn.NO_SUCH_TIMESTAMP);
				return ar;
			} else {
				if (nextElement == null) {
					for (int i = 0; i < timestamps.size(); i++) {
						if (!timestampsUsed[i]) {
							CoveragePlugin.saveLCIToMethodCoverage(method, startLine, endLine, nodeType, false);
							timestampsUsed[i] = true;
							ar.setReturnType(AnalyzeReturn.OK);
							return ar;
						}
					}
				} else if (nextTimestamp == -1) {
					long first = getFirstUnusedTimestamp();
					if (first == -1) {
						ar.setReturnType(AnalyzeReturn.NO_SUCH_TIMESTAMP);
						return ar;
					} else {
						CoveragePlugin.saveLCIToMethodCoverage(method, startLine, endLine, nodeType, false);
						CoveragePlugin.saveLCIToMethodCoverage(method, nextElement.getStartLine(),
								nextElement.getStartLine(), nextElement.getNodeType(), true);
						ar.setReturnType(AnalyzeReturn.THROWS);
						ar.setThrowTS(getFirstUnusedTimestamp());
						CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
						return ar;
					}
				} else {
					long second = getSecondUnusedTimestamp();
					if (second < nextTimestamp && (second != -1)) {
						ar.setThrowTS(getFirstUnusedTimestamp());
						CoveragePlugin.setFirstUnsedTSUsed(timestamps, timestampsUsed);
						CoveragePlugin.saveLCIToMethodCoverage(method, startLine, endLine, nodeType, false);
						CoveragePlugin.saveLCIToMethodCoverage(method, nextElement.getStartLine(),
								nextElement.getStartLine(), nextElement.getNodeType(), true);
						ar.setReturnType(AnalyzeReturn.THROWS);
						return ar;
					}
					for (int i = 0; i < timestamps.size(); i++) {
						if (!timestampsUsed[i]) {
							CoveragePlugin.saveLCIToMethodCoverage(method, startLine, endLine, nodeType, false);
							timestampsUsed[i] = true;
							ar.setReturnType(AnalyzeReturn.OK);
							return ar;
						}
					}
				}
				ar.setReturnType(AnalyzeReturn.NO_SUCH_TIMESTAMP);
				return ar;
			}
		}
		ar.setReturnType(AnalyzeReturn.NO_SUCH_TIMESTAMP);
		return ar;
	}

	@Override
	public void searchAndTagReduntTS(long timestamp) {
		for (int i = 0; i < timestamps.size(); i++) {
			if (!timestampsUsed[i]) {
				if (timestamps.get(i) == timestamp) {
					timestampsUsed[i] = true;
					break;
				}
			}
		}
	}

	@Override
	public long getFirstUnusedTimestamp() {
		if (timestamps != null) {
			for (int i = 0; i < timestamps.size(); i++) {
				if (!timestampsUsed[i]) {
					return timestamps.get(i);
				}
			}
		}
		return -1;
	}

	@Override
	public long getSecondUnusedTimestamp() {
		if (timestamps != null) {
			int counter = 0;
			for (int i = 0; i < timestamps.size(); i++) {
				if (!timestampsUsed[i]) {
					if (counter == 1) {
						return timestamps.get(i);
					}
					counter++;
				}
			}
		}
		return -1;
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
}
