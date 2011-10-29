package event;

import util.EventUtil;

public interface EventTest {

	String TEST_ALL = EventUtil.name("*");

	String TEST_START = EventUtil.name("START");

	String TEST_STOP = EventUtil.name("STOP");

}
