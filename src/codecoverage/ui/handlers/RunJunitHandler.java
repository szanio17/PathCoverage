
package codecoverage.ui.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;

import codecoverage.launching.CoverageTools;
import codecoverage.ui.parts.CoverageView;
import codecoverage.ui.util.ProjectParser;

public class RunJunitHandler {
	@Inject
	private IEventBroker broker;
	@Inject
	private UISynchronize ui;

	@Execute
	public Object execute(IWorkbench workbench, @Named(IServiceConstants.ACTIVE_SHELL) Shell s)
			throws PartInitException {
		IJavaProject javaProject = ProjectParser.getCurrentJavaProject(workbench);
		if (javaProject == null) {
			showDialog(s, "No active java project or editor opened");
			return null;
		}
		IPath path = ProjectParser.getCurrentFilePath(workbench);
		CoverageTools.getMethodAnalyzer().setJavaProject(javaProject);
		CoverageTools.getJUnitRunner().runJUnitTest(javaProject, path, false, false);
		new Job("Button Pusher") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				ui.asyncExec(new Runnable() {
					@Override
					public void run() {
						broker.post("refresh", "refresh view");
					}
				});
				return Status.OK_STATUS;
			}
		}.schedule(800);
		workbench.getActiveWorkbenchWindow().getActivePage().showView(CoverageView.ID);
		return null;
	}

	private void showDialog(Shell s, String text) {
		MessageDialog.openInformation(s, "CodeCoverage", text);
	}

}