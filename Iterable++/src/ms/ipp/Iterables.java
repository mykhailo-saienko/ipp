package ms.ipp;

import static java.util.Arrays.asList;
import static ms.ipp.Algorithms.toKV;
import static ms.ipp.Streams.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ms.ipp.iterable.BiIterable;
import ms.ipp.iterable.FilteredIterable;
import ms.ipp.iterable.MappedIterable;
import ms.ipp.iterable.ProxyBiIterable;
import ms.ipp.iterator.FilteredIterator;

/**
 * @author saienko
 * 
 */
public class Iterables {
	private static final String SEPARATORS = ",";

	public static Consumer<?> NULL_FUNC = e -> {
	};

	public static Runnable NULL_CALL = () -> {
	};

	public static <T, U, V, W> BiIterable<V, W> mapped(BiIterable<T, U> source,
			BiFunction<? super T, ? super U, Entry<V, W>> transform) {
		return toBiIt(mapped(removeProxies(source), toKV(transform)));
	}

	public static <T, U> BiIterable<T, U> filtered(BiIterable<T, U> source, BiPredicate<? super T, ? super U> filter) {
		return filter == null ? source : toBiIt(filtered(removeProxies(source), toKV(filter)));
	}

	public static <T, U> BiIterable<T, U> distinct(BiIterable<T, U> source,
			BiFunction<? super T, ? super U, ?> keyExtractor) {
		return toBiIt(distinct(removeProxies(source), toKV(keyExtractor)));
	}

	public static <T, U> BiIterable<T, U> deleteHook(BiIterable<T, U> source,
			BiConsumer<? super T, ? super U> onDelete) {
		return toBiIt(deleteHook(removeProxies(source), toKV(onDelete)));
	}

	@SuppressWarnings("unchecked")
	public static <T, U> BiIterable<T, U> toBiIt(Iterable<Entry<T, U>> source) {
		if (source == null) {
			return null;
		} else if (source instanceof BiIterable) {
			return (BiIterable<T, U>) source;
		} else {
			return new ProxyBiIterable<>(source);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T, U> Iterable<Entry<T, U>> removeProxies(Iterable<Entry<T, U>> source) {
		while (source instanceof ProxyBiIterable) {
			source = ((ProxyBiIterable<T, U>) source).getSource();
		}
		return source;
	}

	public static <T, U> boolean all(BiIterable<T, U> it, BiPredicate<? super T, ? super U> pred) {
		return all(it, toKV(pred));
	}

	public static <T, U> boolean none(BiIterable<T, U> it, BiPredicate<? super T, ? super U> pred) {
		return none(it, toKV(pred));
	}

	public static <T, U> boolean any(BiIterable<T, U> it, BiPredicate<? super T, ? super U> pred) {
		return any(it, toKV(pred));
	}

	public static <T, U> Entry<T, U> unique(BiIterable<T, U> it, BiPredicate<? super T, ? super U> pred) {
		return unique(it, toKV(pred));
	}

	public static <T, U> Entry<T, U> first(BiIterable<T, U> it, BiPredicate<? super T, ? super U> pred) {
		return first(it, toKV(pred));
	}

	public static <T, U> void forAll(BiIterable<T, U> it, BiConsumer<? super T, ? super U> proc) {
		forAll(it, proc);
	}

	public static <T, U> boolean removeFrom(BiIterable<T, U> it, BiPredicate<? super T, ? super U> pred) {
		return removeFrom(it, toKV(pred));
	}

	public static <T, U> List<Entry<T, U>> list(BiIterable<T, U> it, BiPredicate<? super T, ? super U> pred) {
		return list(it, toKV(pred));
	}

	///// ************** Modifiers for Iterable ***************** /////
	public static <T, U> Iterable<U> mapped(Iterable<T> source, Function<? super T, U> transform) {
		return new MappedIterable<>(source, transform);
	}

	public static <T> Iterable<T> distinct(Iterable<T> source, Function<? super T, ?> keyExtractor) {
		// we only return a predicate-supplier. Otherwise, the "seen" set in the
		// distinctByKey-method would be instantiated now and not upon calling
		// the iterator()-method
		return filtered(source, () -> FilteredIterator.distinctByKey(keyExtractor));
	}

	private static <T> Iterable<T> filtered(Iterable<T> source, Supplier<Predicate<? super T>> filter) {
		return new FilteredIterable<>(source).setFilter(filter);
	}

	public static <T> Iterable<T> filtered(Iterable<T> source, Predicate<? super T> filter) {
		return filter == null ? source : filtered(source, () -> filter);
	}

	public static <T> FilteredIterable<T> deleteHook(Iterable<T> source, Consumer<? super T> onDelete) {
		return new FilteredIterable<>(source).setOnDelete(onDelete);
	}

	public static <T> Iterable<T> toIterable(Iterator<T> it) {
		return toIterable(() -> it, 0);
	}

	public static <T> Iterable<T> toIterable(Supplier<Iterator<T>> it) {
		return toIterable(it, 0);
	}

	public static <T> Iterable<T> toIterable(Supplier<Iterator<T>> it, int chars) {
		Iterable<T> itr = new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return it.get();
			}

			@Override
			public Spliterator<T> spliterator() {
				return Spliterators.spliteratorUnknownSize(iterator(), chars);
			}
		};
		return itr;
	}

	///// ************** Shortcuts for Iterable ***************** /////
	public static <T> boolean all(Iterable<T> it, Predicate<? super T> pred) {
		return Streams.all(stream(it), pred);
	}

	public static <T> boolean none(Iterable<T> it, Predicate<? super T> pred) {
		return Streams.none(stream(it), pred);
	}

	public static <T> boolean any(Iterable<T> it, Predicate<? super T> pred) {
		return Streams.any(stream(it), pred);
	}

	/**
	 * If there is exactly one item in the collection which satisfies a given pred,
	 * returns it. If there are no items satisfying the pred, returns null. If there
	 * are more than one items, throws {@link IllegalArgumentException}.
	 * 
	 * @param items
	 * @param pred
	 * @return
	 */
	public static <T> T unique(Iterable<T> it, Predicate<? super T> pred) {
		return Streams.unique(stream(it), pred);
	}

	public static <T> T first(Iterable<T> it, Predicate<? super T> pred) {
		return Streams.first(stream(it), pred);
	}

	public static <T> void forAll(Iterable<T> it, Consumer<? super T> proc) {
		// we don't have to create streams for this
		if (it != null) {
			it.forEach(proc);
		}
	}

	public static <T> boolean removeFrom(Iterable<T> it, Predicate<? super T> pred) {
		boolean removed = false;
		Iterator<T> itr = it.iterator();
		if (pred == null) {
			pred = t -> true;
		}
		while (itr.hasNext()) {
			if (pred.test(itr.next())) {
				itr.remove();
				removed = true;
			}
		}
		return removed;
	}

	public static <T> int count(Iterable<T> it) {
		return list(it).size();
	}

	public static <T, U extends T> List<T> list(Iterable<U> it) {
		return list(it, null);
	}

	public static <T, U extends T> List<T> list(Iterable<U> it, Predicate<? super U> pred) {
		return Streams.list(stream(it), pred);
	}

	public static <T, U extends T> Set<T> set(Iterable<U> it, Predicate<? super U> pred) {
		return Streams.set(stream(it), pred);
	}

	/**
	 * Creates a Map<T,U> for Iterables over subclasses of Entry<T,U>
	 * 
	 * @param it
	 * @param pred
	 * @return
	 */
	public static <T, U> Map<T, U> map(Iterable<? extends Entry<T, U>> it) {
		return collect(it, null, Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	public static <T, R> R collect(Iterable<T> it, Predicate<? super T> pred, Collector<T, ?, R> collector) {
		return Streams.collect(stream(it), pred, collector);
	}

	public static <T, R> R collect(Iterable<T> it, Predicate<? super T> pred, Collector<T, ?, R> collector, int n) {
		return Streams.collect(stream(it), pred, collector, n);
	}

	///// ************** Shortcuts for Collections ***************** /////
	public static <T, U> List<U> map(Collection<T> items, Function<T, U> mapper) {
		return mapFilter(items, mapper, null);
	}

	public static <T, U> List<U> filterMap(Collection<T> items, Predicate<T> pred, Function<T, U> mapper) {
		return Streams.list(Streams.mapped(Streams.filtered(stream(items), pred), mapper), null);
	}

	public static <T, U> List<U> mapFilter(Collection<T> items, Function<T, U> mapper, Predicate<U> pred) {
		return Streams.list(Streams.mapped(stream(items), mapper), pred);
	}

	public static <T> int count(Collection<T> it) {
		return it.size();
	}

	///// ************ Shortcuts for Collection serializing ************** /////
	public static <T> String toString(T[] array) {
		StringBuffer buf = new StringBuffer(500);
		appendList(buf, Arrays.asList(array));
		return buf.toString();
	}

	public static void appendList(StringBuffer buf, Collection<?> list) {
		appendList(buf, list, (t, b) -> b.append(t.toString()));
	}

	public static <T> void appendList(StringBuffer buf, Collection<? extends T> list,
			BiConsumer<T, StringBuffer> serializer) {
		appendList(buf, list, "[", "]", ", ", serializer);
	}

	public static <T> void appendList(StringBuffer buf, Collection<? extends T> list, String op, String cl, String sep,
			BiConsumer<T, StringBuffer> serializer) {
		buf.append(op);

		for (T t : list) {
			serializer.accept(t, buf);
			buf.append(sep);
		}

		if (list.size() >= 1) {
			buf.delete(buf.length() - sep.length(), buf.length());
		}
		buf.append(cl);
	}

	public static <T> String appendList(Collection<? extends T> list, String op, String cl, String sep,
			Function<T, String> serializer) {
		return appendList(list, op, cl, sep, (s, sb) -> sb.append(serializer.apply(s)));
	}

	public static <T> String appendList(Collection<? extends T> list, String op, String cl, String sep,
			BiConsumer<T, StringBuffer> serializer) {
		StringBuffer sb = new StringBuffer(list.size() * 1000);
		appendList(sb, list, op, cl, sep, serializer);
		return sb.toString();
	}

	///// ************ Shortcuts for Lists ************** /////
	public static <T> int indexOf(List<T> list, Predicate<T> pred) {
		if (list == null) {
			return -1;
		}
		for (int i = 0; i < list.size(); ++i) {
			if (pred.test(list.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public static <T> int lastIndexOf(List<T> list, Predicate<T> pred) {
		if (list == null) {
			return -1;
		}
		for (int i = list.size() - 1; i >= 0; --i) {
			if (pred.test(list.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public static <U> int[] getIndexes(List<U> totalList, List<U> subList) {
		totalList = totalList == null ? new ArrayList<>() : totalList;
		return totalList.stream().mapToInt(s -> subList.indexOf(s)).toArray();
	}

	///// ************ Shortcuts for Maps ************** /////
	public static <T> void ifExistsDo(String name, Map<String, ? extends Object> attrs, Consumer<T> proc) {
		ifExistsDo(name, attrs, null, proc);
	}

	@SuppressWarnings("unchecked")
	public static <T> void ifExistsDo(String name, Map<String, ? extends Object> attrs, List<String> ignore,
			Consumer<T> proc) {
		if (attrs.containsKey(name) && (ignore == null || !ignore.contains(name))) {
			proc.accept((T) attrs.get(name));
		}
	}

	public static <T, U> List<U> getInsert(T key, Map<T, List<U>> map) {
		return getInsert(key, map, () -> new ArrayList<>());
	}

	public static <T, U> U getInsert(T key, Map<T, U> map, Supplier<U> onNull) {
		U value = map.get(key);
		if (value == null) {
			value = onNull.get();
			map.put(key, value);
		}
		return value;
	}

	public static <T> T get(Object key, Map<?, ?> attrs) {
		return get(key, attrs, (T) null);
	}

	public static <T> T get(Object key, Map<?, ?> attrs, T def) {
		return get(key, attrs, () -> def);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(Object key, Map<?, ?> attrs, Supplier<T> def) {
		Object obj = attrs.get(key);
		try {
			if (obj != null) {
				return (T) obj;
			} else {
				return def.get();
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(
					"Cannot convert attribute " + obj + " of type " + obj.getClass().getSimpleName());
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T remove(Object key, Map<?, ?> attrs, Supplier<T> def) {
		Object obj = attrs.remove(key);
		try {
			if (obj != null) {
				return (T) obj;
			} else {
				return def.get();
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(
					"Cannot convert attribute " + obj + " of type " + obj.getClass().getSimpleName());
		}
	}

	public static <T, U> Map<T, U> asMap(T[] a, U[] b) {
		if (a == null || b == null) {
			throw new IllegalArgumentException("Key or value array is null");
		}

		Map<T, U> map = new HashMap<>();
		for (int i = 0; i < a.length; ++i) {
			T key = a[i];
			U value = i >= b.length ? null : b[i];
			map.put(key, value);
		}
		return map;
	}

	public static <T, V> Map<T, V> makeMap(T[] names, V[] values) {
		Map<T, V> map = new HashMap<T, V>();
		if (names == null || values == null) {
			throw new IllegalArgumentException("Names and values must both be not null");
		}
		if (names.length != values.length) {
			throw new IllegalArgumentException("Names and values must be of the same length! Names has length "
					+ names.length + "; values - " + values.length);
		}
		for (int i = 0; i < names.length; i++) {
			map.put(names[i], values[i]);
		}
		return map;
	}

	public static <K, V> Map<K, V> filter(Map<K, V> map, BiPredicate<? super K, ? super V> entryPred) {
		Map<K, V> result = new HashMap<>();
		for (Map.Entry<K, V> e : map.entrySet()) {
			if (entryPred.test(e.getKey(), e.getValue())) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}

	/**
	 * Returns a map of the same type as the original with keys modified by the
	 * keyMapper
	 * 
	 * @param items
	 * @param keyMapper
	 * @return
	 */
	public static <T, U, V> Map<U, V> map(Map<T, V> items, Function<T, U> keyMapper) {
		if (items == null) {
			return null;
		}
		Map<U, V> target = items instanceof TreeMap ? new TreeMap<>() : new HashMap<>();

		for (Map.Entry<T, V> e : items.entrySet()) {
			target.put(keyMapper.apply(e.getKey()), e.getValue());
		}
		return target;
	}

	public static <T, U> TreeMap<Date, U> transform(Map<Date, T> source, Function<T, U> transformer) {
		TreeMap<Date, U> result = new TreeMap<>();
		for (Map.Entry<Date, T> e : source.entrySet()) {
			result.put(e.getKey(), transformer.apply(e.getValue()));
		}
		return result;
	}

	///// ************ Shortcuts for String Parsing ************** /////
	// TODO: What is it doing here?
	public static int replaceToken(StringBuffer buffer, String tokenName, String tokenValue) {
		int index = 0;
		int count = 0;
		while ((index = buffer.indexOf(tokenName, index)) != -1) {
			buffer.replace(index, index + tokenName.length(), tokenValue);
			count++;
		}
		return count;
	}

	public static List<String> makeList(String source) {
		return makeList(source, SEPARATORS);
	}

	public static List<String> makeList(String source, String regex) {
		return (source != null) ? asList(source.split(regex)) : new ArrayList<String>();
	}

	public static <T> List<T> parseList(List<String> source, Function<String, T> parser) {
		return parseList(source, (s, res) -> res.add(parser.apply(s)));
	}

	public static <T> List<T> parseList(List<String> source, BiConsumer<String, List<T>> parser) {
		if (source == null) {
			throw new IllegalArgumentException("Can not convert a null list");
		}
		List<T> result = new ArrayList<>();
		for (String s : source) {
			try {
				parser.accept(s, result);
			} catch (Exception e) {
				throw new IllegalArgumentException("Parameter list " + source + " cannot be parsed");
			}
		}
		return result;
	}

	///// ************ Miscellaneous Helpers ************** /////

	/**
	 * Returns a symmetric difference of two sets, i.e. a set containing elements
	 * from either s1 or s2 but filters out those that lie in both sets.
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static <T> Set<T> symmDiff(final Collection<? extends T> s1, final Collection<? extends T> s2) {
		Set<T> symmetricDiff = new HashSet<T>(s1);
		symmetricDiff.addAll(s2);
		Set<T> tmp = new HashSet<T>(s1);
		tmp.retainAll(s2);
		symmetricDiff.removeAll(tmp);
		return symmetricDiff;
	}

	public static <T> List<T> intersection(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<T>();
		// lookups are O(1) in a set and O(n) in a list. So convert to a set.
		// It is better to have 2*O(n) than O(n^2).
		Set<T> set2 = new HashSet<>(list2);
		for (T t : list1) {
			if (set2.contains(t)) {
				list.add(t);
			}
		}

		return list;
	}

	public static <T> List<T> union(Collection<T> list1, Collection<T> list2) {
		Set<T> temp = new HashSet<>();
		temp.addAll(list1);
		temp.addAll(list2);
		return new ArrayList<>(temp);
	}

	public static <T> Set<T> asSet(T... a) {
		return Stream.of(a).collect(Collectors.toSet());
	}

	public static boolean isEqualOrNull(Object o1, Object o2) {
		return (o1 == null && o1 == o2) || (o1 != null && o1.equals(o2));
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<List<T>> listClass() {
		return (Class<List<T>>) new ArrayList<T>().getClass();
	}
}
