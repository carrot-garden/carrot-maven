/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench;

import java.util.concurrent.Executor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Property;
import org.osgi.service.component.annotations.Reference;

@Component( //
		name = "hello", factory = "factory hello", servicefactory = true)
public class Comp0 implements Runnable, Cloneable, Executor {

	@Property
	static final String KEY = "key";

	@Property
	static final String VALUE = "value";

	@Reference
	protected void bind(final Runnable task) {
	}

	protected void unbind(final Runnable task) {
	}

	@Reference
	protected void bind(final Cloneable task) {
	}

	protected void unbind(final Cloneable task) {
	}

	@Reference
	protected void bind(final Executor task) {
	}

	protected void unbind(final Executor task) {
	}

	@Override
	public void run() {
	}

	@Override
	public void execute(final Runnable command) {
	}

}
