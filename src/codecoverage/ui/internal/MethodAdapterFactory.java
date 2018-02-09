package codecoverage.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;

import codecoverage.view.Method;

public class MethodAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object o, Class type) {
		if (type == IPropertySource.class && o instanceof Method) {
			return new MethodPropertySource((Method) o);
		} else {
			return null;
		}
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IPropertySource.class };
	}

}
