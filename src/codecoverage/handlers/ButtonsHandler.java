package codecoverage.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.launcher.JUnitMigrationDelegate;
import org.eclipse.jdt.internal.junit.launcher.TestKindRegistry;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.analysis.IfProbes;

import codecoverage.core.CoreData;
import codecoverage.core.ExecutionDataServer;
import codecoverage.core.MethodASTVisitor;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.coverage.LineCoverageInfo;
import codecoverage.elements.MethodElement;
import codecoverage.parsers.ProjectParser;
import codecoverage.view.Class;
import codecoverage.view.Method;
import codecoverage.view.OwnView;
import codecoverage.view.Project;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ButtonsHandler extends AbstractHandler {

	private static Map<String, List<Boolean>> hashOfExe = new HashMap<String, List<Boolean>>();
	private static List<String> listOfPackages = null;
	private static Map<String, IClassCoverage> classCoverages;
	private boolean firstStep = true;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		switch (event.getCommand().getId()) {
		case "runJUnit":
			runJUnitTest(event, false, false);
			break;
		case "analysis":
			runJUnitTest(event, false, true);
			break;
		case "runJUnitLast":
			runJUnitTest(event, true, false);
			break;
		}
		// DebugPlugin.getDefault().removeDebugEventFilter(eventFilter);
		return null;

	}

	private void analyzeMethods(ExecutionEvent event) {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(ExecutionDataServer.store, coverageBuilder);
		IJavaProject javaProject = ProjectParser.getCurrentJavaProject(event);
		IfProbes.setOffline(false);
		IfProbes.resetUsedTimestamps();
		System.out.println(javaProject.getProject().getLocation());
		try {
			analyzer.analyzeAll(new File(javaProject.getProject().getLocation() + "/bin"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<IType> classes = new ArrayList<>();
		classCoverages = new HashMap<String, IClassCoverage>();
		for (IClassCoverage cc : coverageBuilder.getClasses()) {
			try {

				String className = cc.getName().replaceAll("/", ".");
				IType findedType = javaProject.findType(className);
				if (findedType != null) {
					classes.add(findedType);
					classCoverages.put(className, cc);
				} else {
					System.out.println("type: null");
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		List<MethodElement> listOfMethod = new ArrayList<>();
		// CompilationUnit cu2 = null;
		// MethodElement me2 = null;
		Project p = new Project();
		p.setName(javaProject.getElementName());
		List<Class> listOfClasses = new ArrayList<Class>();
		Map<String, Class> mapOfClasses = new HashMap<String, Class>();
		for (IType type : classes) {

			// IType iType = ProjectParser.findOpenCurrentClass(javaProject,
			// false);
			ICompilationUnit iCompilationUnit = type.getCompilationUnit();
			CompilationUnit cu = parse(iCompilationUnit);

			ASTNode rootNode = cu.getRoot();
			Class c = new Class();
			c.setFullName(type.getFullyQualifiedName());
			c.setProject(p);
			listOfClasses.add(c);
			mapOfClasses.put(type.getFullyQualifiedName(), c);

			List<Method> listOfMethods = new ArrayList<Method>();
			Map<String, Method> mapOfMethods = new HashMap<String, Method>();

			rootNode.accept(new ASTVisitor() {

				@Override
				public boolean visit(MethodDeclaration node) {
					SimpleName name = node.getName();

					Method m = new Method();
					m.setClass1(c);
					m.setName(name.getIdentifier());
					m.setPathCoverages(new HashMap<Integer, List<Integer>>());
					m.setLinesCoverage(new HashMap<Integer, LineCoverageInfo>());
					listOfMethods.add(m);
					mapOfMethods.put(m.getName(), m);

					MethodElement me = new MethodElement();
					me.setName(name.getIdentifier());
					me.setThisNode(node);
					me.setCu(cu);
					me.setMethod(m);
					me.setClassName(type.getFullyQualifiedName());
					me.setStartLine(cu.getLineNumber(name.getStartPosition()));
					me.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
					listOfMethod.add(me);
					return false;
				}
			});
			Method[] methods = new Method[listOfMethods.size()];
			for (int i = 0; i < methods.length; i++) {
				methods[i] = listOfMethods.get(i);
			}
			c.setMethods(methods);

		}
		Class[] classesL = new Class[listOfClasses.size()];
		for (int i = 0; i < classesL.length; i++) {
			classesL[i] = listOfClasses.get(i);
		}
		p.setClasses(classesL);
		CoveragePlugin.updateProjectTree(p);

		for (MethodElement me : listOfMethod) {
			System.out.println("-----------");
			System.out.println(me.getClassName() + "." + me.getName());
			MethodASTVisitor mVisitor = new MethodASTVisitor(me, classCoverages.get(me.getClassName()));
			// MethodASTVisitor2 mVisitor2 = new MethodASTVisitor2(me,
			// me.getCu(), null); // for
			// testing,
			// how
			// AST
			// works
			// me.getThisNode().accept(mVisitor2);
			me.getThisNode().accept(mVisitor);
			me.analyze(null, null, -1);
			int c = CoveragePlugin.calcCountOfPathCoverage(me.getMethod().getPathCoverages());
			me.getMethod().setActualPathCoverages(c);
			int max = me.getCountBranches();
			me.getMethod().setMaxPathCoverages(max);
		}
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(OwnView.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});

	}

	protected CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit); // set source
		parser.setResolveBindings(true); // we need bindings later on
		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
	}

	private void runAnalysisData() {
		ExecutionDataStore store = ExecutionDataServer.store;
		for (ExecutionData data : store.getContents()) {
			if (isInUsersProject(data.getName())) {
				// System.out.println("runanalysisData:" + data.getId());
				List<Boolean> list = new ArrayList<Boolean>();
				for (boolean bool : data.getProbes()) {
					list.add(bool);
				}
				hashOfExe.put(data.getName(), list);
			}
		}
	}

	private void printExeData() {
		System.out.println("Analysis");
		System.out.println("--------");
		Set<String> list = hashOfExe.keySet();
		for (String pa : list) {
			System.out.println(pa);
			for (boolean bool : hashOfExe.get(pa)) {
				System.out.println("\t" + bool);
			}
		}
		// Together with the original class definition we can calculate coverage
		// information:
		IfProbes.setOffline(false);
		IfProbes.resetUsedTimestamps();
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(ExecutionDataServer.store, coverageBuilder);
		try {
			analyzer.analyzeAll(new File(ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/Testing/bin/main"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Extended analysis");
		CoreData.coverageBuilder = coverageBuilder;
		// Let's dump some metrics and line coverage information:
		for (final IClassCoverage cc : coverageBuilder.getClasses()) {
			System.out.println();
			System.out.println(cc.getName());
			printCounter("instructions", cc.getInstructionCounter());
			printCounter("branches", cc.getBranchCounter());
			printCounter("lines", cc.getLineCounter());
			printCounter("methods", cc.getMethodCounter());
			printCounter("complexity", cc.getComplexityCounter());
			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
				System.out.printf("Line %s: %s", Integer.valueOf(i), getColor(cc.getLine(i).getStatus()));
				if (cc.getLinesTimestamps(i) != null) {
					System.out.println("," + cc.getLinesTimestamps(i));
				} else {
					System.out.println();
				}
			}
		}
	}

	private void printCounter(final String unit, final ICounter counter) {
		final Integer missed = Integer.valueOf(counter.getMissedCount());
		final Integer total = Integer.valueOf(counter.getTotalCount());
		System.out.printf("%s of %s %s missed%n", missed, total, unit);
	}

	// private InputStream getTargetClass(final String name) {
	// final String resource = '/' + name.replace('.', '/') + ".class";
	// return getClass().getResourceAsStream(resource);
	// }

	private String getColor(final int status) {
		switch (status) {
		case ICounter.NOT_COVERED:
			return "red";
		case ICounter.PARTLY_COVERED:
			return "yellow";
		case ICounter.FULLY_COVERED:
			return "green";
		}
		return "";
	}

	private boolean isInUsersProject(String name) {
		for (String pa : listOfPackages) {
			if (name.startsWith(pa)) {
				return true;
			}
		}
		return false;
	}

	private void runJUnitTest(ExecutionEvent event, boolean isLast, boolean linesData) {

		IfProbes.resetAll();
		IJavaProject javaProject = ProjectParser.getCurrentJavaProject(event);

		if (isLast && ProjectParser.lastClassPath == null) {
			showDialog("No test was start yet", event);
			return;
		}

		listOfPackages = ProjectParser.getPackages(javaProject);

		IType classType = ProjectParser.findOpenCurrentClass(javaProject, isLast);
		if (classType == null) {
			showDialog("Cant find class.", event);
			return;
		}

		Thread t = new Thread(() -> {
			try {
				ExecutionDataServer.run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		t.start();

		try {
			ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration(classType, event);
			wc.launch(ILaunchManager.RUN_MODE, null);
			if (firstStep) {
				DebugPlugin.getDefault().addDebugEventFilter(new IDebugEventFilter() {

					@Override
					public DebugEvent[] filterDebugEvents(DebugEvent[] arg0) {
						if (arg0[0].getKind() == DebugEvent.TERMINATE) {

							if (linesData) {
								runAnalysisData();
								printExeData();
							} else {
								analyzeMethods(event);
							}
						}
						return arg0;
					}
				});
				firstStep = false;
			}

			ProjectParser.saveLastClassPath();
		} catch (CoreException e1) {
			e1.printStackTrace();
			showDialog("No JUnit test was found in current class", event);
		}
	}

	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IJavaElement element, ExecutionEvent event)
			throws CoreException {
		final String testName;
		final String mainTypeQualifiedName;
		final String containerHandleId;
		switch (element.getElementType()) {
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
		case IJavaElement.PACKAGE_FRAGMENT: {
			String name = element.getElementName();
			containerHandleId = element.getHandleIdentifier();
			mainTypeQualifiedName = "";
			testName = name.substring(name.lastIndexOf(IPath.SEPARATOR) + 1);
		}
			break;
		case IJavaElement.TYPE: {
			containerHandleId = "";
			mainTypeQualifiedName = ((IType) element).getFullyQualifiedName('.'); // don't
																					// replace,
																					// fix
																					// for
																					// binary
																					// inner
																					// types
			testName = element.getElementName();
		}
			break;
		case IJavaElement.METHOD: {
			IMethod method = (IMethod) element;
			containerHandleId = "";
			mainTypeQualifiedName = method.getDeclaringType().getFullyQualifiedName('.');
			testName = method.getDeclaringType().getElementName() + '.' + method.getElementName();
		}
			break;
		default:
			throw new IllegalArgumentException(
					"Invalid element type to create a launch configuration: " + element.getClass().getName()); //$NON-NLS-1$
		}

		String testKindId = TestKindRegistry.getContainerTestKindId(element);
		ILaunchConfigurationType configType = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(JUnitLaunchConfigurationConstants.ID_JUNIT_APPLICATION);
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
				DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(testName));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeQualifiedName);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, element.getJavaProject().getElementName());
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, containerHandleId);
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND, testKindId);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				"-javaagent:" + ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/jacocoagent.jar"
						+ "=output=tcpclient,address=localhost,port=9999");
		System.out.println();
		JUnitMigrationDelegate.mapResources(wc);
		// AssertionVMArg.setArgDefault(wc);
		if (element instanceof IMethod) {
			wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_METHOD_NAME, element.getElementName()); // only
																												// set
																												// for
																												// methods
		}
		return wc;
	}

	public static void showDialog(String text, ExecutionEvent event) {
		IWorkbenchWindow window = null;
		try {
			window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		MessageDialog.openInformation(window.getShell(), "CodeCoverage", text);
	}
}
