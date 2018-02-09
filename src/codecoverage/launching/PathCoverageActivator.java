package codecoverage.launching;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PathCoverageActivator extends AbstractUIPlugin {

	private static PathCoverageActivator instance;

	private IDebugEventSetListener debugListener = new IDebugEventSetListener() {
		@Override
		public void handleDebugEvents(DebugEvent[] events) {
			for (final DebugEvent e : events) {
				if (e.getSource() instanceof IProcess && e.getKind() == DebugEvent.TERMINATE) {
					final IProcess proc = (IProcess) e.getSource();
					final ILaunch launch = proc.getLaunch();
					try {
						if (launch.getLaunchConfiguration()
								.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS, "PathCoverage")
								.equals("PathCoverage")) {
							methodAnalyzer.analyzeMethods();
						}
					} catch (CoreException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	};
	private JUnitRunner junitRunner;

	private MethodAnalyzer methodAnalyzer;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		System.out.println("Im in activator of plugin");
		junitRunner = new JUnitRunner();
		methodAnalyzer = new MethodAnalyzer();
		DebugPlugin.getDefault().addDebugEventListener(debugListener);
		instance = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		instance = null;
		DebugPlugin.getDefault().removeDebugEventListener(debugListener);
		junitRunner = null;
		methodAnalyzer = null;
		super.stop(context);
	}

	public static PathCoverageActivator getInstance() {
		return instance;
	}

	public JUnitRunner getJunitRunner() {
		return junitRunner;
	}

	public MethodAnalyzer getMethodAnalyzer() {
		return methodAnalyzer;
	}

}
