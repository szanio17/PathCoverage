package codecoverage.core;

import java.util.List;

import codecoverage.view.Method;

public interface ICodeElement {

	public void analyze(int deep);

	public int getCountBranches();

	public int getStartLine();

	public int getEndLine();

	public int getNodeType();

	public long getFirstUnusedTimestamp();

	public List<Long> getTimestamps();

	public void setTimestamps(List<Long> timestamps);

	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS);

	public long getSecondUnusedTimestamp();

	public void searchAndTagReduntTS(long timestamp);

	public static boolean testThrows(ICodeElement element, ICodeElement nextElement) {

		return false;
	}
}
