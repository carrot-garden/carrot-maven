/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package event;

import util.EventUtil;

public interface EventTest {

	String TEST_ALL = EventUtil.name("*");

	String TEST_START = EventUtil.name("START");

	String TEST_STOP = EventUtil.name("STOP");

}
