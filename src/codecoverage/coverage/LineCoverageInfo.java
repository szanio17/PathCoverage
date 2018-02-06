package codecoverage.coverage;

public class LineCoverageInfo {
	private int startLine;
	private int endLine;
	private int nodeType;
	private int count = 0;
	private boolean abstractCode;

	public void incCount() {
		count++;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public int getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}

	public boolean isAbstractCode() {
		return abstractCode;
	}

	public void setAbstractCode(boolean abstractCode) {
		this.abstractCode = abstractCode;
	}

}
