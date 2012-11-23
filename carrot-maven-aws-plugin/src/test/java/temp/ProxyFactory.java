package temp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProxyFactory {

	@SuppressWarnings("unchecked")
	public static <T> T makeProxy(final Class<T> face, final Object instance,
			final Object instanceProxy) {

		return (T) Proxy.newProxyInstance( //

				face.getClassLoader(), //

				new Class[] { face }, //

				new InvocationHandler() {

					private final Map<MethodEntry, Method> //
					methodProxyMap = new HashMap<MethodEntry, Method>();

					/** find all methods in adapter object */

					{

						final Method[] methodArray = instanceProxy.getClass()
								.getDeclaredMethods();

						for (final Method method : methodArray) {
							methodProxyMap.put(new MethodEntry(method), method);
						}

					}

					@Override
					public Object invoke( //
							final Object proxy, //
							final Method method, //
							final Object[] args //
					) throws Throwable {

						try {

							final Method methodProxy = methodProxyMap
									.get(new MethodEntry(method));

							if (methodProxy == null) {
								return method.invoke(instance, args);
							} else {
								return methodProxy.invoke(instanceProxy, args);
							}

						} catch (final InvocationTargetException e) {
							throw e.getTargetException();
						}
					}

				}

		);

	}

	private static class MethodEntry {

		private final String methodName;
		private final Class<?>[] parameterTypes;

		public MethodEntry(final Method m) {
			methodName = m.getName();
			parameterTypes = m.getParameterTypes();
		}

		@Override
		public boolean equals(final Object other) {

			final MethodEntry entry = (MethodEntry) other;

			return methodName.equals(entry.methodName)
					&& Arrays.equals(parameterTypes, entry.parameterTypes);

		}

		@Override
		public int hashCode() {

			return methodName.hashCode();

		}

	}

}
