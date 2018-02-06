package codecoverage.view;

import java.util.List;
import java.util.Map;

import codecoverage.coverage.LineCoverageInfo;

public class PrintMethodInfo {
	private Method method;

	public PrintMethodInfo(Method method) {
		super();
		this.method = method;
	}

	public void printMethodInfo() {
		System.out.println("Project name: " + method.getClass1().getProject().getName());
		System.out.println("Class name: " + method.getClass1().getName() + " " + method.getClass1().getFullName());
		System.out.println("Method name: " + method.getName());

		System.out.println("MaxPathCoverages " + method.getMaxPathCoverages());
		System.out.println("ActualPathCoverages " + method.getActualPathCoverages());
		System.out.println("CountOfCoverages " + method.getCountOfCoverages());
		System.out.println("Start Line " + method.getStartLine());
		Map<Integer, LineCoverageInfo> linesCoverage = method.getLinesCoverage();
		Map<Integer, List<Integer>> pathCoverages = method.getPathCoverages();
		System.out.println("Size of map pathCoverages " + pathCoverages.size());
		System.out.println("Size of map linesCoverage " + linesCoverage.size());
		System.out.println("Printing all path coverages ");
		for (int j = 1; j <= pathCoverages.size(); j++) {
			List<Integer> listOfLines = pathCoverages.get(j);
			System.out.println("Coverage number " + j);
			for (Integer i : listOfLines) {
				LineCoverageInfo lci = linesCoverage.get(i);
				System.out.printf("Line " + i + " ");
				System.out.printf("start " + lci.getStartLine() + " ");
				System.out.printf("end " + lci.getEndLine() + " ");
				System.out.printf("abstract " + lci.isAbstractCode() + " ");
				System.out.printf("count " + lci.getCount() + " ");
				System.out.println("nodeType " + lci.getNodeType() + " ");
			}
		}

	}
}
