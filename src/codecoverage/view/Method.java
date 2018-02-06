package codecoverage.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import codecoverage.coverage.LineCoverageInfo;

public class Method {
	private Class class1;
	private String name;
	//private List<Integer> pathCoverage;
	private Map<Integer, List<Integer>> pathCoverages;
	private Map<Integer, LineCoverageInfo> linesCoverage;
	private int countOfCoverages = 0;
	private int actualPathCoverages = 0;
	private int maxPathCoverages = 0;
	private int startLine = 0;
	
	public Method() {

	}
	
	public void incCountOfCoverages() {
		countOfCoverages++;
		pathCoverages.put(countOfCoverages, new ArrayList<Integer>());
	}
	
	public Class getClass1() {
		return class1;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public void setClass1(Class class1) {
		this.class1 = class1;
	}

	public List<Integer> getPathCoverage(int numberOfCoverage) {
		return pathCoverages.get(numberOfCoverage);
	}
	
	public Map<Integer, LineCoverageInfo> getLinesCoverage() {
		return linesCoverage;
	}

	public void setLinesCoverage(Map<Integer, LineCoverageInfo> linesCoverage) {
		this.linesCoverage = linesCoverage;
	}

	public String getName() {
		return name;
	}

	public Map<Integer, List<Integer>> getPathCoverages() {
		return pathCoverages;
	}

	public void setPathCoverages(Map<Integer, List<Integer>> pathCoverages) {
		this.pathCoverages = pathCoverages;
	}
	
	public List<Integer> getPathCoverage() {
		return pathCoverages.get(countOfCoverages);
	}

	public int getCountOfCoverages() {
		return countOfCoverages;
	}

	public void setCountOfCoverages(int countOfCoverages) {
		this.countOfCoverages = countOfCoverages;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getActualPathCoverages() {
		return actualPathCoverages;
	}

	public void setActualPathCoverages(int actualPathCoverages) {
		this.actualPathCoverages = actualPathCoverages;
	}

	public int getMaxPathCoverages() {
		return maxPathCoverages;
	}

	public void setMaxPathCoverages(int maxPathCoverages) {
		this.maxPathCoverages = maxPathCoverages;
	}
}
