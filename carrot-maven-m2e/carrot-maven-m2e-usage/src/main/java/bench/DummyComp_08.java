package bench;

import java.util.concurrent.Executor;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;

@Component
public class DummyComp_08 {

	@Reference
	Executor executor;

}
