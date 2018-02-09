package codecoverage.ui.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.log.LogService;

import codecoverage.coverage.CoveragePlugin;
import codecoverage.view.Method;

public class CoverageView {
	private TreeViewer treeViewer;
	private TreeViewerColumn column0;
	public static final String ID = "test.partDescFragment.ASampleE4View";
	@Inject
	private LogService logService;
	@Inject
	private ISharedImages images;
	@Inject
	ESelectionService selectionService;
	ISelectionChangedListener selectionListener;

	@PostConstruct
	public void createPartControl(Composite parent, EMenuService menu) {
		if (!menu.registerContextMenu(parent, "codecoverage.ui.menu.0")) {
			logService.log(LogService.LOG_ERROR, "Failed to register pop-up menu");
		}
		System.out.println("Enter in SampleE4View postConstruct");
		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		treeViewer = new TreeViewer(tree);
		column0 = new TreeViewerColumn(treeViewer, SWT.LEFT);
		column0.getColumn().setText("Elements");
		column0.getColumn().setWidth(300);
		column0.setLabelProvider(new ProjectLabelProvider(images));

		treeViewer.setContentProvider(new ProjectContentProvider());
		treeViewer.setInput(CoveragePlugin.getListOfProjects());
		selectionListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				if (selectionService != null) {
					IStructuredSelection thisSelection = (IStructuredSelection) e.getSelection();
					Object selectedNode = thisSelection.getFirstElement();
					if (selectedNode instanceof Method) {
						selectionService.setSelection(selectedNode);
					}
				}

			}
		};
		treeViewer.addSelectionChangedListener(selectionListener);
		treeViewer.expandAll();
	}

	@Focus
	public void setFocus() {
		treeViewer.getControl().setFocus();

	}

	@Inject
	@Optional
	public void receiveEvent(@UIEventTopic("error/launch") String data) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MessageDialog.openConfirm(window.getShell(), "CodeCoverage", data);
	}

	@Inject
	@Optional
	public void refreshViewer(@UIEventTopic("refresh") String data) {
		treeViewer.setInput(CoveragePlugin.getListOfProjects());
		treeViewer.refresh();
		treeViewer.refresh(column0);
	}

	/**
	 * This method is kept for E3 compatiblity. You can remove it if you do not
	 * mix E3 and E4 code. <br/>
	 * With E4 code you will set directly the selection in ESelectionService and
	 * you do not receive a ISelection
	 * 
	 * @param s
	 *            the selection received from JFace (E3 mode)
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection s) {
		if (s.isEmpty())
			return;

		if (s instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) s;
			if (iss.size() == 1)
				setSelection(iss.getFirstElement());
			else
				setSelection(iss.toArray());
		}
	}

	/**
	 * This method manages the selection of your current object. In this example
	 * we listen to a single Object (even the ISelection already captured in E3
	 * mode). <br/>
	 * You should change the parameter type of your received Object to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current object received
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object o) {

		// Remove the 2 following lines in pure E4 mode, keep them in mixed mode
		if (o instanceof ISelection) // Already captured
			return;

		// Test if label exists (inject methods are called before PostConstruct)
		// if (myLabelInView != null)
		// myLabelInView.setText("Current single selection class is : " +
		// o.getClass());
	}

	/**
	 * This method manages the multiple selection of your current objects. <br/>
	 * You should change the parameter type of your array of Objects to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current array of objects received in case of multiple
	 *            selection
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object[] selectedObjects) {

		// Test if label exists (inject methods are called before PostConstruct)
		// if (myLabelInView != null)
		// myLabelInView.setText("This is a multiple selection of " +
		// selectedObjects.length + " objects");
	}
}
