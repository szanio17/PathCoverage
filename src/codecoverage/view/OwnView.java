package codecoverage.view;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;

import codecoverage.annotations.AnnotationPlugin;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.frames.CoverageFrame;
import codecoverage.parsers.ProjectParser;

public class OwnView extends ViewPart {
	Action addItemAction, deleteItemAction, selectAllAction;
	ListViewer viewer;
	public static final String ID = "codecoverage.view.coverageview";
	private TreeViewer m_treeViewer;
	private static OwnView ownViewInstance;

	public OwnView() {
		super();
		ownViewInstance = this;

	}

	@Override
	public void setFocus() {
		m_treeViewer.getControl().setFocus();

	}

	// @SuppressWarnings("unchecked")
	// public void updateProjectTree(Project project) {
	// Object o = m_treeViewer.getInput();
	// if (o instanceof List<?>) {
	// List<Project> list = (List<Project>) o;
	// Project removeProject = null;
	// for (Project p : list) {
	// if (p.getName().equals(project.getName())) {
	// removeProject = p;
	// break;
	// }
	// }
	// if(removeProject != null) {
	// list.remove(removeProject);
	// list.add(0, project);
	// }
	// m_treeViewer.setInput(m_treeViewer.getInput());
	// }
	// else {
	// List<Project> list = new ArrayList<Project>();
	// list.add(project);
	// m_treeViewer.setInput(list);
	// }
	//
	// }

	@Override
	public void createPartControl(Composite parent) {
		Tree addressTree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		addressTree.setHeaderVisible(true);
		m_treeViewer = new TreeViewer(addressTree);
		TreeColumn column1 = new TreeColumn(addressTree, SWT.LEFT);
		addressTree.setLinesVisible(true);
		column1.setAlignment(SWT.LEFT);
		column1.setText("Project-Class-Method");
		column1.setWidth(160);

		m_treeViewer.setContentProvider(new ProjectContentProvider());
		m_treeViewer.setLabelProvider(new TableLabelProvider());

		m_treeViewer.setInput(CoveragePlugin.getListOfProjects());
		m_treeViewer.expandAll();
		m_treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {

				TreeViewer viewer = (TreeViewer) event.getViewer();
				IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
				Object selectedNode = thisSelection.getFirstElement();

				viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));
				if (selectedNode instanceof Method) {
					System.out.println(selectedNode);
					Method m = (Method) selectedNode;
					CoverageFrame cf = new CoverageFrame("Path coverage", m, AnnotationPlugin.getEditor());
					PrintMethodInfo info = new PrintMethodInfo(m);
					info.printMethodInfo();
					ProjectParser.openEditorOfGivenProject(m.getClass1().getFullName(),
							m.getClass1().getProject().getName());
					cf.setVisible(true);
				}
			}
		});
	}

	class ProjectContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof List)
				return ((List<?>) parentElement).toArray();
			if (parentElement instanceof Project)
				return ((Project) parentElement).getClasses();
			if (parentElement instanceof Class)
				return ((Class) parentElement).getMethods();
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof Class)
				return ((Class) element).getProject();
			if (element instanceof Method)
				return ((Method) element).getClass1();
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof List)
				return ((List<?>) element).size() > 0;
			if (element instanceof Project)
				return ((Project) element).getClasses().length > 0;
			if (element instanceof Class)
				return ((Class) element).getMethods().length > 0;
			return false;
		}

		@Override
		public Object[] getElements(Object obj) {
			return getChildren(obj);
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}
	}

	class TableLabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return element.toString();
			}
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {

		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {

		}

	}

	public static OwnView getOwnViewInstance() {
		return ownViewInstance;
	}

	public static void setOwnViewInstance(OwnView ownViewInstance) {
		OwnView.ownViewInstance = ownViewInstance;
	}

	public TreeViewer getM_treeViewer() {
		return m_treeViewer;
	}

	public void setM_treeViewer(TreeViewer m_treeViewer) {
		this.m_treeViewer = m_treeViewer;
	}

}
