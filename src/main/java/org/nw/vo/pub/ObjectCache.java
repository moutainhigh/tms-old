package org.nw.vo.pub;

import java.util.LinkedHashMap;
import java.util.Map;

import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

public class ObjectCache {

	// SIZE必须小于一个Byte的值 256
	private final static int SIZE = 50;

	private RLRUMap<Integer, UFDate> read_date_map = new RLRUMap<Integer, UFDate>(SIZE);

	private RLRUMap<Integer, UFDateTime> read_datet_map = new RLRUMap<Integer, UFDateTime>(SIZE);;

	private RLRUMap<Integer, UFDouble> read_dbl_map = new RLRUMap<Integer, UFDouble>(SIZE);

	private WLRUMap<UFDate, Integer> date_map = new WLRUMap<UFDate, Integer>(SIZE);

	private WLRUMap<UFDateTime, Integer> datet_map = new WLRUMap<UFDateTime, Integer>(SIZE);

	private WLRUMap<UFDouble, Integer> dbl_map = new WLRUMap<UFDouble, Integer>(SIZE);

	private class WLRUMap<K, V> extends LinkedHashMap<K, V> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private int intCapacity = 10;

		private V removeValue;

		WLRUMap(int initialCapacity) {
			super(initialCapacity, 0.75f, true);
			this.intCapacity = initialCapacity;
		}

			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			if(size() > this.intCapacity) {
				removeValue = eldest.getValue();
				return true;
			} else
				return false;
		}

			public V put(K key, V value) {
			super.put(key, value);
			if(removeValue != null)
				super.put(key, removeValue);
			return null;
		}

	}

	// 对与反序列化的LRUMap中如果超出SIZE，临时存入改数的KEY
	private final static int TEMP_READKEY = 1000;

	private class RLRUMap<K, V> extends LinkedHashMap<K, V> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2L;

		private int intCapacity = 10;

		private K removedKey;

		RLRUMap(int initialCapacity) {
			super(initialCapacity, 0.75f, true);
			this.intCapacity = initialCapacity;
		}

			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			if(size() > this.intCapacity) {
				removedKey = eldest.getKey();
				return true;
			} else
				return false;
		}

			public V put(K key, V value) {
			super.put(key, value);
			if(removedKey != null) {
				V tmpVal = super.get(TEMP_READKEY);
				super.remove(TEMP_READKEY);
				super.put(removedKey, tmpVal);
			}
			return null;
		}

	}

	public int getWriteUFDate(UFDate date) {
		return date_map.get(date) == null ? -1 : date_map.get(date);
	}

	public void putWriteUFDate(UFDate date) {
		date_map.put(date, date_map.size());
	}

	public int getWriteUFDateTime(UFDateTime datet) {
		return datet_map.get(datet) == null ? -1 : datet_map.get(datet);
	}

	public void putWriteUFDateTime(UFDateTime datet) {
		datet_map.put(datet, datet_map.size());
	}

	public int getWriteUFDouble(UFDouble dbl) {
		return dbl_map.get(dbl) == null ? -1 : dbl_map.get(dbl);
	}

	public void putWriteUFDouble(UFDouble dbl) {
		dbl_map.put(dbl, dbl_map.size());
	}

	/*
	 * 读的处理
	 */
	public UFDate getReadUFDate(int index) {
		return read_date_map.get(index);
	}

	public void putReadUFDate(UFDate date) {
		if(read_date_map.size() == SIZE)
			read_date_map.put(TEMP_READKEY, date);
		else
			read_date_map.put(read_date_map.size(), date);
	}

	public UFDateTime getReadUFDateTime(int index) {
		return read_datet_map.get(index);
	}

	public void putReadUFDateTime(UFDateTime datet) {
		if(read_datet_map.size() == SIZE)
			read_datet_map.put(TEMP_READKEY, datet);
		else
			read_datet_map.put(read_datet_map.size(), datet);
	}

	public UFDouble getReadUFDouble(int index) {
		return read_dbl_map.get(index);
	}

	public void putReadUFDouble(UFDouble dbl) {
		if(read_dbl_map.size() == SIZE)
			read_dbl_map.put(TEMP_READKEY, dbl);
		else
			read_dbl_map.put(read_dbl_map.size(), dbl);
	}

	public static void main(String[] args) {
		ObjectCache test = new ObjectCache();
		test.testSize();
		test.testReadSize();
	}

	@SuppressWarnings("unchecked")
	public void testSize() {
		System.out.println("WRITE RESULT:");
		ObjectCache o1 = new ObjectCache();

		o1.putWriteUFDate(new UFDate("2008-04-20"));
		o1.putWriteUFDate(new UFDate("2008-04-21"));
		o1.putWriteUFDate(new UFDate("2008-04-22"));
		o1.putWriteUFDate(new UFDate("2008-04-23"));

		for(UFDate elem : o1.date_map.keySet().toArray(new UFDate[0])) {

			System.out.println(elem + ":" + o1.date_map.get(elem));
		}
		System.out.println(o1.getWriteUFDate(new UFDate("2008-04-21")));
		o1.putWriteUFDate(new UFDate("2008-04-25"));
		for(UFDate elem : o1.date_map.keySet().toArray(new UFDate[0])) {

			System.out.println(elem + ":" + o1.date_map.get(elem));
		}

		o1.putWriteUFDouble(new UFDouble("10.23"));
		o1.putWriteUFDouble(new UFDouble("10.24"));
		o1.putWriteUFDouble(new UFDouble("10.25"));
		o1.putWriteUFDouble(new UFDouble("10.26"));

		for(UFDouble elem : o1.dbl_map.keySet().toArray(new UFDouble[0])) {

			System.out.println(elem + ":" + o1.dbl_map.get(elem));
		}
		System.out.println(o1.getWriteUFDouble(new UFDouble("10.24")));
		o1.putWriteUFDouble(new UFDouble("10.27"));
		for(UFDouble elem : o1.dbl_map.keySet().toArray(new UFDouble[0])) {

			System.out.println(elem + ":" + o1.dbl_map.get(elem));
		}
	}

	public void testReadSize() {
		System.out.println("READ...RESULT");
		ObjectCache o1 = new ObjectCache();

		o1.putReadUFDate(new UFDate("2008-04-20"));
		o1.putReadUFDate(new UFDate("2008-04-21"));
		o1.putReadUFDate(new UFDate("2008-04-22"));
		o1.putReadUFDate(new UFDate("2008-04-23"));

		for(Integer elem : o1.read_date_map.keySet().toArray(new Integer[0])) {

			System.out.println(elem + ":" + o1.read_date_map.get(elem));
		}
		System.out.println(o1.getReadUFDate(1));
		o1.putReadUFDate(new UFDate("2008-04-25"));
		for(Integer elem : o1.read_date_map.keySet().toArray(new Integer[0])) {

			System.out.println(elem + ":" + o1.read_date_map.get(elem));
		}
	}

}
