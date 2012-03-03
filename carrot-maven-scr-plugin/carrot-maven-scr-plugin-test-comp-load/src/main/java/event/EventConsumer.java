package event;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Property;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

@Component
public class EventConsumer implements EventHandler {

	@Property(name = "event.topics")
	static final String EVENT = EventTest.TEST_START;

	@Override
	public void handleEvent(final Event event) {

	}

}
