package codecoverage.ui.internal;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import codecoverage.view.Method;

public class MethodPropertySource implements IPropertySource {
	private static final Object NAME = new Object();
	private static final Object MAX_COV = new Object();
	private static final Object ACT_COV = new Object();

	private Method method;

	public MethodPropertySource(Method method) {
		this.method = method;
	}

	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { new PropertyDescriptor(NAME, "Method"),
				new PropertyDescriptor(MAX_COV, "Max path coverages"),
				new PropertyDescriptor(ACT_COV, "Actual path coverages") };
	}

	@Override
	public Object getPropertyValue(Object arg0) {
		if (NAME.equals(arg0)) {
			return method.getName();
		} else if (MAX_COV.equals(arg0)) {
			return method.getMaxPathCoverages();
		} else if (ACT_COV.equals(arg0)) {
			return method.getPathCoverages().size();
		} else {
			return null;
		}
	}

	@Override
	public boolean isPropertySet(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetPropertyValue(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyValue(Object arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

}
