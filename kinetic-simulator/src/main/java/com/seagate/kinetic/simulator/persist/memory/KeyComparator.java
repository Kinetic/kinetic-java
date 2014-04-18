package com.seagate.kinetic.simulator.persist.memory;

import java.io.Serializable;
import java.util.Comparator;

public class KeyComparator implements Comparator<byte[]>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2435110880291192149L;

	public KeyComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(byte[] left, byte[] right) {

		int l1 = left.length;
		int l2 = right.length;

		int len = Math.min(l1, l2);

		for (int i = 0; i < len; i++) {
			int a = (left[i] & 0xff);
			int b = (right[i] & 0xff);
			if (a != b) {
				return a - b;
			}
		}

		return left.length - right.length;
	}

}
