
package codecoverage.ui.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;

public class RefreshViewHandler {

	@Inject
	private IEventBroker broker;

	@Execute
	public void execute() {
		broker.post("refresh", "refresh view");
	}

}