/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

//
//
////////////////
//

@Component
public class Comp1 {

	@Activate
	protected void activate1() {
	}

	@Reference
	protected void bind(final Runnable task) {
	}

	protected void unbind(final Runnable task) {

	}

}

//
//
//

