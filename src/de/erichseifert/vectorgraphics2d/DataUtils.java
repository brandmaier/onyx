package de.erichseifert.vectorgraphics2d;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class that contains utility functions for working with data
 * collections like maps or lists.
 */
public abstract class DataUtils {
	/**
	 * Default constructor that prevents creation of class.
	 */
	protected DataUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a mapping from two arrays, one with keys, one with values.
	 * @param <K> Data type of the keys.
	 * @param <V> Data type of the values.
	 * @param keys Array containing the keys.
	 * @param values Array containing the values.
	 * @return Map with keys and values from the specified arrays.
	 */
	public static <K,V> Map<K, V> map(K[] keys, V[] values) {
		// Check for valid parameters
		if (keys.length != values.length) {
			throw new IllegalArgumentException(
					"Number of keys and values is different. " +
					"Cannot create map.");
		}
		// Fill map with keys and values
		Map<K, V> map = new HashMap<K, V>();
		for (int i = 0; i < keys.length; i++) {
			K key = keys[i];
			V value = values[i];
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Returns a string with all float values divided by a specified separator.
	 * @param separator Separator string.
	 * @param elements Float array.
	 * @return Joined string.
	 */
	public static String join(String separator, float... elements) {
		if (elements == null || elements.length == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer(elements.length*3);
		for (int i = 0; i < elements.length; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(elements[i]);
		}
		return sb.toString();
	}

	/**
	 * Returns a string with all float values divided by a specified separator.
	 * @param separator Separator string.
	 * @param elements Double array.
	 * @return Joined string.
	 */
	public static String join(String separator, double... elements) {
		if (elements == null || elements.length == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer(elements.length*3);
		for (int i = 0; i < elements.length; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(elements[i]);
		}
		return sb.toString();
	}
}
