package bench;

class Heavy {

	static void log(final String text) {
		System.out.println(text);
	}

	static {
		log("static heavy");
	}

}
