package codecoverage.elements;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;

import codecoverage.core.AbstractBranchCodeElement;
import codecoverage.core.AnalyzeReturn;
import codecoverage.core.ICodeElement;
import codecoverage.coverage.LineCoverageInfo;
import codecoverage.view.Method;

public class MethodElement extends AbstractBranchCodeElement {

	private String name;

	private CompilationUnit cu;
	private String className;
	private Method method;

	@Override
	public AnalyzeReturn analyze(Method method, ICodeElement nextElement, long nextIterationTS) {
		int code = -1;
		if (blockCode == null) {
			System.out.println("methods body is empty");
		} else {
			while (code != AnalyzeReturn.NO_SUCH_TIMESTAMP) {
				this.method.incCountOfCoverages();
				this.method.getPathCoverage().add(-1);
				// coverageData.add((short)-1);
				long firstTS = blockCode.getSecondUnusedTimestamp();
				AnalyzeReturn ar = blockCode.analyze(this.method, null, firstTS);
				int returnType = ar.getReturnType();
				if (returnType == AnalyzeReturn.NO_SUCH_TIMESTAMP) {
					code = AnalyzeReturn.NO_SUCH_TIMESTAMP;
				}
				// for (int i = startLine; i <= endLine; i++) {
				// if (codeElements.containsKey(new Integer(i))) {
				// ICodeElement el = codeElements.get(new Integer(i));
				// ICodeElement nextElement2 =
				// AbstractBranchCodeElement.getNextCode(codeElements, i,
				// endLine);
				// if (nextElement2 != null) {
				// AnalyzeReturn ar = el.analyze(this.method, nextElement2,
				// nextIterationTS);
				// int returnType = ar.getReturnType();
				// if (returnType == AnalyzeReturn.RETURN || returnType ==
				// AnalyzeReturn.THROW
				// || returnType == AnalyzeReturn.NO_SUCH_TIMESTAMP
				// || returnType == AnalyzeReturn.THROWS) {
				// code = AnalyzeReturn.NO_SUCH_TIMESTAMP;
				// }
				// } else {
				// AnalyzeReturn ar = el.analyze(this.method, null,
				// nextIterationTS);
				// if (ar != null) {
				// int returnType = ar.getReturnType();
				// if (returnType == AnalyzeReturn.RETURN || returnType ==
				// AnalyzeReturn.THROW
				// || returnType == AnalyzeReturn.NO_SUCH_TIMESTAMP
				// || returnType == AnalyzeReturn.THROWS) {
				// code = AnalyzeReturn.NO_SUCH_TIMESTAMP;
				// } else if (returnType == AnalyzeReturn.THROWS) {
				// code = AnalyzeReturn.NO_SUCH_TIMESTAMP;
				// }
				// }
				// }
				// }
				// }
			}
			analyzeListOfCoverage();
		}

		return null;
	}

	public void analyzeListOfCoverage() {
		if (method.getCountOfCoverages() == 1 && method.getPathCoverage(1).size() == 1) {
			method.getPathCoverages().clear();
			method.setCountOfCoverages(0);
		} else {
			for (int i = 1; i <= method.getPathCoverages().size(); i++) {
				List<Integer> list = method.getPathCoverage(i);
				if (list.size() > 1) {
					for (int z = 0; z < list.size(); z++) {
						if (list.get(z) == -1) {
							list.remove(z);
							list.add(z, this.startLine);
							Map<Integer, LineCoverageInfo> map = method.getLinesCoverage();
							LineCoverageInfo lci = null;
							lci = new LineCoverageInfo();
							lci.setStartLine(startLine);
							lci.setEndLine(startLine);
							map.put(startLine, lci);
							method.setStartLine(this.startLine);
						}
					}
				} else {
					if (list.size() == 1) {
						method.getPathCoverages().remove(i);
						method.setCountOfCoverages(method.getCountOfCoverages() - 1);
					}
				}
			}
		}
	}

	@Override
	public int getCountBranches() {
		return blockCode.getCountBranches();
	}

	@Override
	public void analyze(int deep) {
		String tab = "";
		for (int i = 0; i < deep; i++) {
			tab += "	";
		}
		for (int i = startLine; i <= endLine; i++) {
			if (codeElements.containsKey(new Integer(i))) {
				ICodeElement el = codeElements.get(new Integer(i));
				if (el instanceof AbstractBranchCodeElement) {
					System.out.println(tab + el.getClass().getName() + ", start:" + el.getStartLine() + ", end:"
							+ el.getEndLine() + ", times:" + el.getTimestamps());
					AbstractBranchCodeElement element = (AbstractBranchCodeElement) el;
					element.analyze(deep + 1);
				} else {
					el.analyze(deep + 1);
				}
			}
		}
	}

	@Override
	public boolean addElement(ICodeElement element) {
		if (element instanceof BlockCodeElement) {
			if (blockCode == null) {
				blockCode = element;
				return true;
			}
			// return false;
			// if (!codeElements.containsKey(new
			// Integer(element.getStartLine()))) {
			// codeElements.put(new Integer(element.getStartLine()), element);
			// return true;
			// }
		} else if (element.getStartLine() != startLine && element.getStartLine() > startLine) {
			if (blockCode == null) {
				blockCode = element;
				return true;
			}
			// if (!codeElements.containsKey(new
			// Integer(element.getStartLine()))) {
			// codeElements.put(new Integer(element.getStartLine()), element);
			// return true;
			// }
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CompilationUnit getCu() {
		return cu;
	}

	public void setCu(CompilationUnit cu) {
		this.cu = cu;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

}
