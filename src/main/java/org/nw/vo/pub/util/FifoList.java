package org.nw.vo.pub.util;

import java.util.LinkedList;

public class FifoList extends LinkedList {
	int capacity;

	public  FifoList(int capacity) {
		this.capacity = capacity;
	}

	public void add(int index, Object element) {
		if (size() > capacity)
			removeFirst();

		super.add(index, element);

	}

}