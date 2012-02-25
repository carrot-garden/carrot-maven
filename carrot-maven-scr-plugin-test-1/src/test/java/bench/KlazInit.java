package bench;

public class KlazInit {

	static void log(final String text) {
		System.out.println(text);
	}

	public static void main(final String[] args) throws Exception {

		log("init");

		final ClassLoader loader = KlazInit.class.getClassLoader();

		//

		log("init=false");

		final Class<?> klaz1 = Class.forName("bench.Heavy", false, loader);

		log("klaz1=" + klaz1);
		log("klaz1=" + klaz1.hashCode());

		//

		log("init=true");

		final Class<?> klaz2 = Class.forName("bench.Heavy", true, loader);

		log("klaz2=" + klaz2);
		log("klaz2=" + klaz2.hashCode());

		//

		log("done");

	}

}
