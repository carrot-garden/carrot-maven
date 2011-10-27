package anno;

import bench.Comp1;

import com.carrotgarden.osgi.anno.scr.make.Maker;

public class AnnotaRunner {

	public static void main(final String[] args) throws Exception {

		final boolean is = Maker.isAnnotationPresent(Comp1.class);

		System.out.println("is : " + is);

	}

}
