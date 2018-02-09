package codecoverage.coverage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import codecoverage.view.Method;
import codecoverage.view.OwnView;
import codecoverage.view.Project;

public class CoveragePlugin {
	private static List<Project> listOfProjects;

	public static int calcCountOfPathCoverage(Map<Integer, List<Integer>> coverage) {
		boolean[] unique = new boolean[coverage.size() + 1];
		Arrays.fill(unique, true);
		int countUnique = 0;
		for (int i = 1; i <= coverage.size(); i++) {
			List<Integer> cov1 = coverage.get(i);
			if (unique[i]) {
				if (i + 1 > coverage.size()) {
					countUnique++;
				} else {
					for (int z = 1 + i; z <= coverage.size(); z++) {
						List<Integer> cov2 = coverage.get(z);
						boolean found = false;
						if (cov1.size() != cov2.size()) {
							found = true;
						} else {
							for (int index = 0; index < cov1.size(); index++) {
								if (cov1.get(index) != cov2.get(index)) {
									found = true;
									break;
								}
							}
						}
						if (!found) {
							if (z < unique.length) {
								unique[z] = false;
							}
						}
						countUnique++;
					}
				}
			}
		}
		return countUnique;
	}

	public static long getFirstUnusedTS(List<Long> timestamps, boolean[] timestampsUsed) {
		if (timestamps != null) {
			for (int i = 0; i < timestamps.size(); i++) {
				if (!timestampsUsed[i]) {
					return timestamps.get(i);
				}
			}
		}
		return -1;
	}

	public static void setFirstUnsedTSUsed(List<Long> timestamps, boolean[] timestampsUsed) {
		for (int i = 0; i < timestamps.size(); i++) {
			if (!timestampsUsed[i]) {
				timestampsUsed[i] = true;
				break;
			}
		}
	}

	public static void saveLCIToMethodCoverage(Method method, int startLine, int endLine, int nodeType,
			boolean isThrowCode) {
		saveLCIToMethodCoverage(method, startLine, endLine, nodeType, isThrowCode, false, true);
	}

	public static void saveLCIToMethodCoverage(Method method, int startLine, int endLine, int nodeType,
			boolean isThrowCode, boolean isAbstractCode, boolean incCount) {
		int throwMulti = isThrowCode ? -1 : 1;
		method.getPathCoverage().add(startLine * throwMulti);
		Map<Integer, LineCoverageInfo> map = method.getLinesCoverage();
		LineCoverageInfo lci = null;
		if (!map.containsKey(startLine)) {
			lci = new LineCoverageInfo();
			lci.setStartLine(startLine);
			lci.setEndLine(startLine);
			lci.setNodeType(nodeType);
			lci.setAbstractCode(isAbstractCode);
			map.put(startLine, lci);
		} else {
			lci = map.get(startLine);
		}
		if (incCount) {
			lci.incCount();
		}

	}

	public static void setListOfProjects(List<Project> listOfProjects) {
		CoveragePlugin.listOfProjects = listOfProjects;
	}

	public static void updateProjectTree(Project project) {
		if (listOfProjects == null) {
			listOfProjects = new ArrayList<Project>();
			listOfProjects.add(project);
		} else {
			Project removeProject = null;
			for (Project p : listOfProjects) {
				if (p.getName().equals(project.getName())) {
					removeProject = p;
					break;
				}
			}
			if (removeProject != null) {
				listOfProjects.remove(removeProject);
				listOfProjects.add(0, project);
			}
		}
		OwnView ov = OwnView.getOwnViewInstance();
		if (ov != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					ov.getM_treeViewer().setInput(listOfProjects);
					Object service = PlatformUI.getWorkbench().getService(IEventBroker.class);
					if (service instanceof IEventBroker)
						((IEventBroker) service).post("refresh", "kero");
				}
			});
		}

	}

	public static LineCoverageInfo createLCI(int startLine, int endLine) {
		LineCoverageInfo lci = new LineCoverageInfo();
		lci.setStartLine(startLine);
		lci.setEndLine(endLine);
		return lci;
	}

	public static List<Project> getListOfProjects() {
		if (listOfProjects == null) {
			List<Project> list = null;
			// Project p = new Project();
			// p.setName("No coverage found.");
			// Class[] c = new Class[0];
			// p.setClasses(c);
			// list.add(p);
			return list;
		} else {
			return listOfProjects;
		}
	}
}
