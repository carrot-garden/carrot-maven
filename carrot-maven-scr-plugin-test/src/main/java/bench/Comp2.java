package bench;

import java.util.concurrent.Callable;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Property;
import org.osgi.service.component.annotations.Reference;

@Component(property = { "hello-string:String=hello string" })
public class Comp2 extends Comp1 implements Runnable {

	@Property
	private static final String hello = "hello there";

	@Property(name = "good-bye")
	protected static final String goodBye = "see you later";

	@Property(name = "multi-lines")
	static final String MULTI = "\n one \n two \n";

	@Reference
	protected void bind(final Callable task) {

	}

	protected void unbind(final Callable task) {

	}

	@Override
	public void run() {
	}

	@Deactivate
	protected void deactivate2() {
	}

}
