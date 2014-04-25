/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
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
