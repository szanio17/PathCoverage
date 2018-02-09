package codecoverage.launching;

public final class CoverageTools {

	public static MethodAnalyzer getMethodAnalyzer() {
		return PathCoverageActivator.getInstance().getMethodAnalyzer();
	}

	public static JUnitRunner getJUnitRunner() {
		return PathCoverageActivator.getInstance().getJunitRunner();
	}
}
