package de.luckymcdev.foundryengine.common.util;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionHelper {

	@SuppressWarnings("unchecked")
	public static <T> Class<T> classForName(String className) {
		try {
			return (Class<T>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class '" + className + "' not found", e);
		}
	}

	public static Field getDeclaredField(Class<?> clazz, String fieldName) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Field '" + fieldName + "' not found in " + clazz.getName(), e);
		}
	}

	public static Field findField(Class<?> clazz, String fieldName) {
		Class<?> current = clazz;
		while (current != null) {
			try {
				Field field = current.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
		}
		throw new RuntimeException("Field '" + fieldName + "' not found in " + clazz.getName() + " or any superclass");
	}

	public static <T> @Nullable T getFieldValue(Field field, @Nullable Object target) {
		try {
			@SuppressWarnings("unchecked")
			T result = (T) field.get(target);
			return result;
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot read field '" + field.getName() + "'", e);
		}
	}

	public static void setFieldValue(Field field, @Nullable Object target, @Nullable Object value) {
		try {
			field.set(target, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot write field '" + field.getName() + "'", e);
		}
	}

	public static <T> @Nullable T getFieldValue(Class<?> clazz, @Nullable Object target, String fieldName) {
		return getFieldValue(findField(clazz, fieldName), target);
	}

	public static void setFieldValue(Class<?> clazz, @Nullable Object target, String fieldName, @Nullable Object value) {
		setFieldValue(findField(clazz, fieldName), target, value);
	}

	public static <T> @Nullable T getStaticFieldValue(Class<?> clazz, String fieldName) {
		return getFieldValue(clazz, null, fieldName);
	}

	public static void setStaticFieldValue(Class<?> clazz, String fieldName, @Nullable Object value) {
		setFieldValue(clazz, null, fieldName, value);
	}

	public static <T> @Nullable T getFieldValue(Object target, String fieldName) {
		return getFieldValue(findField(target.getClass(), fieldName), target);
	}

	public static void setFieldValue(Object target, String fieldName, @Nullable Object value) {
		setFieldValue(findField(target.getClass(), fieldName), target, value);
	}

	public static <T> @Nullable T getFieldValue(String className, @Nullable Object target, String fieldName) {
		return getFieldValue(classForName(className), target, fieldName);
	}

	public static void setFieldValue(String className, @Nullable Object target, String fieldName, @Nullable Object value) {
		setFieldValue(classForName(className), target, fieldName, value);
	}

	public static <T> @Nullable T getStaticFieldValue(String className, String fieldName) {
		return getFieldValue(classForName(className), null, fieldName);
	}

	public static void setStaticFieldValue(String className, String fieldName, @Nullable Object value) {
		setFieldValue(classForName(className), null, fieldName, value);
	}

	public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			Method method = clazz.getDeclaredMethod(methodName, paramTypes);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Method '" + methodName + "' not found in " + clazz.getName(), e);
		}
	}

	@Nullable
	public static Object invoke(Method method, @Nullable Object target, @Nullable Object... args) {
		try {
			return method.invoke(target, args);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Cannot invoke method '" + method.getName() + "'", e);
		}
	}

	@Nullable
	public static Object invoke(Class<?> clazz, @Nullable Object target, String methodName, @Nullable Object... args) {
		Method method = resolveMethod(clazz, methodName, args);
		return invoke(method, target, args);
	}

	@Nullable
	public static Object invokeStatic(Class<?> clazz, String methodName, @Nullable Object... args) {
		return invoke(clazz, null, methodName, args);
	}

	@Nullable
	public static Object invoke(Object target, String methodName, @Nullable Object... args) {
		return invoke(target.getClass(), target, methodName, args);
	}

	@Nullable
	public static Object invoke(String className, @Nullable Object target, String methodName, @Nullable Object... args) {
		return invoke(classForName(className), target, methodName, args);
	}

	@Nullable
	public static Object invokeStatic(String className, String methodName, @Nullable Object... args) {
		return invoke(classForName(className), null, methodName, args);
	}

	private static Method resolveMethod(Class<?> clazz, String methodName, @Nullable Object... args) {
		int argCount = args != null ? args.length : 0;
		List<Method> candidates = new ArrayList<>();

		for (Method method : clazz.getDeclaredMethods()) {
			if (!method.getName().equals(methodName)) {
				continue;
			}
			if (method.getParameterCount() != argCount) {
				continue;
			}
			candidates.add(method);
		}

		if (candidates.isEmpty()) {
			throw new RuntimeException("Method '" + methodName + "' with " + argCount + " args not found in " + clazz.getName());
		}

		if (candidates.size() == 1) {
			Method method = candidates.getFirst();
			method.setAccessible(true);
			return method;
		}

		// Try exact type match
		if (args != null) {
			for (Method method : candidates) {
				Class<?>[] params = method.getParameterTypes();
				boolean match = true;
				for (int i = 0; i < argCount; i++) {
					if (args[i] != null && !params[i].isInstance(args[i])) {
						match = false;
						break;
					}
				}
				if (match) {
					method.setAccessible(true);
					return method;
				}
			}
		}

		// Fall back to first candidate
		Method method = candidates.getFirst();
		method.setAccessible(true);
		return method;
	}

	public static <T> T newInstance(Class<T> clazz, @Nullable Object... args) {
		try {
			Class<?>[] paramTypes = new Class<?>[args != null ? args.length : 0];
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
				}
			}
			Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);
			constructor.setAccessible(true);
			return constructor.newInstance(args);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Cannot create instance of " + clazz.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String className, @Nullable Object... args) {
		return newInstance(classForName(className), args);
	}
}
