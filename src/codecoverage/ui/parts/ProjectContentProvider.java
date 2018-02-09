package codecoverage.ui.parts;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import codecoverage.view.Class;
import codecoverage.view.Method;
import codecoverage.view.Project;

public class ProjectContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object arg0) {
		if (arg0 instanceof List)
			return ((List<?>) arg0).toArray();
		if (arg0 instanceof Project)
			return ((Project) arg0).getClasses();
		if (arg0 instanceof Class)
			return ((Class) arg0).getMethods();
		return new Object[0];
	}

	@Override
	public Object[] getElements(Object arg0) {
		return getChildren(arg0);
	}

	@Override
	public Object getParent(Object arg0) {
		if (arg0 instanceof Class)
			return ((Class) arg0).getProject();
		if (arg0 instanceof Method)
			return ((Method) arg0).getClass1();
		return null;
	}

	@Override
	public boolean hasChildren(Object arg0) {
		if (arg0 instanceof List)
			return ((List<?>) arg0).size() > 0;
		if (arg0 instanceof Project)
			return ((Project) arg0).getClasses().length > 0;
		if (arg0 instanceof Class)
			return ((Class) arg0).getMethods().length > 0;
		return false;
	}

}
