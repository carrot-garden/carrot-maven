package bench;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class Comp1 {

	@Reference
	protected void bind(final Runnable task) {

	}

	protected void unbind(final Runnable task) {

	}

}
