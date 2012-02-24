package util;

import java.util.Map;

public interface EventService {

	void post(String topic);

	void send(String topic);

	//

	void post(String topic, Map<String, String> props);

	void send(String topic, Map<String, String> props);

}
