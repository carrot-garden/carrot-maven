/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench;

import java.util.Map;
import java.util.concurrent.Callable;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Property;
import org.osgi.service.component.annotations.Reference;

@Component(property = { "hello-string:String=hello string" })
public class Comp2 extends Comp1 implements Runnable {

	@SuppressWarnings("unused")
	@Property
	private static final String AAA = "aaa aaa aaa";

	@SuppressWarnings("unused")
	@Property
	private static final String hello = "hello there";

	@Property(name = "good-bye")
	protected static final String goodBye = "see you later";

	@Property(name = "multi-lines")
	static final String MULTI = "\n one \n two \n";

	//

	@Reference
	protected void bind(final Callable<?> task, final Map<?, ?> param) {
	}

	protected void unbind(final Callable<?> task, final Map<?, ?> param) {
	}

	//

	@Override
	public void run() {
		//
	}

	@Deactivate
	protected void deactivate2() {
	}

}
