package util;

import java.util.Map;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import util.ClassUtil.ClassTrace;


public final class EventUtil {

	private EventUtil() {
		//
	}

	public static String filterPath(final String path) {
		return path.replace('.', '/').replace('$', '/');
	}

	public static String filterAction(final String action) {
		return action.replace('/', '_').replace('$', '-');
	}

	// osgi v 4.3 $ 113.3.1
	public static String name(final Class<?> klaz, final String act) {
		final String name = klaz.getName();
		final String path = filterPath(name);
		final String action = filterPath(act);
		final String event = path + "/" + action;
		return event;
	}

	public static String name(final String action) {
		final ClassTrace trace = new ClassUtil.ClassTrace();
		final Class<?> klaz = trace.getClassAt(3);
		return name(klaz, action);
	}

	public static void post(final EventAdmin eventAdmin, final String eventName) {
		post(eventAdmin, eventName, null);
	}

	public static void send(final EventAdmin eventAdmin, final String eventName) {
		send(eventAdmin, eventName, null);
	}

	public static void post(final EventAdmin eventAdmin,
			final String eventName, final Map<String, String> eventProps) {
		eventAdmin.postEvent(new Event(eventName, eventProps));
	}

	public static void send(final EventAdmin eventAdmin,
			final String eventName, final Map<String, String> eventProps) {
		eventAdmin.sendEvent(new Event(eventName, eventProps));
	}

	public static boolean is(final Event event, final String... nameArray) {
		if (event == null || nameArray == null) {
			return false;
		}
		final String topic = event.getTopic();
		for (final String name : nameArray) {
			if (topic.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static String getProperty(final Event event, final String name) {
		return (String) event.getProperty(name);
	}

}
