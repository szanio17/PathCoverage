package codecoverage.launching;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.launcher.JUnitMigrationDelegate;
import org.eclipse.jdt.internal.junit.launcher.TestKindRegistry;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.ui.PlatformUI;
import org.jacoco.core.internal.analysis.IfProbes;

import codecoverage.core.ExecutionDataServer;
import codecoverage.parsers.ProjectParser;

public class JUnitRunner {
	private IEventBroker broker;
	private static List<String> listOfPackages;

	public JUnitRunner() {
		super();
		listOfPackages = null;
		// TODO Auto-generated constructor stub
	}

	public void runJUnitTest(IJavaProject javaProject, IPath path, boolean isLast, boolean linesData) {
		Object service = PlatformUI.getWorkbench().getService(IEventBroker.class);
		if (service instanceof IEventBroker)
			broker = (IEventBroker) service;
		IfProbes.resetAll();

		if (isLast && ProjectParser.lastClassPath == null) {
			broker.post("error/launch", "No test was start yet");
			return;
		}

		listOfPackages = ProjectParser.getPackages(javaProject);

		IType classType = ProjectParser.findOpenCurrentClass(javaProject, path, isLast);
		if (classType == null) {
			broker.post("error/launch", "Cant find class.");
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
			ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration(classType);
			wc.launch(ILaunchManager.RUN_MODE, null);
			ProjectParser.saveLastClassPath();
		} catch (CoreException e1) {
			e1.printStackTrace();
			broker.post("error/launch", "No JUnit test was found in current class");
		}
	}

	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IJavaElement element) throws CoreException {
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
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS, "PathCoverage");
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, containerHandleId);
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND, testKindId);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				"-javaagent:" + ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/jacocoagent.jar"
						+ "=output=tcpclient,address=localhost,port=9999");
		System.out.println(ResourcesPlugin.getWorkspace().getRoot().getLocation());
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
}
