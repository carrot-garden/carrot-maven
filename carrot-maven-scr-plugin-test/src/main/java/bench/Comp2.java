package bench;

import java.util.concurrent.Callable;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class Comp2 extends Comp1 {

	@Reference
	protected void bind(final Callable task) {

	}

	protected void unbind(final Callable task) {

	}

}
