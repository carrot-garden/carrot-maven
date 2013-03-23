/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package util;

import java.util.Map;

public interface EventService {

	void post(String topic);

	void send(String topic);

	//

	void post(String topic, Map<String, String> props);

	void send(String topic, Map<String, String> props);

}
