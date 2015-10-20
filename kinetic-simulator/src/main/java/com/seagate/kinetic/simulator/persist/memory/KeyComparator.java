/**
 * Copyright 2013-2015 Seagate Technology LLC.
 *
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at
 * https://mozilla.org/MP:/2.0/.
 * 
 * This program is distributed in the hope that it will be useful,
 * but is provided AS-IS, WITHOUT ANY WARRANTY; including without 
 * the implied warranty of MERCHANTABILITY, NON-INFRINGEMENT or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the Mozilla Public 
 * License for more details.
 *
 * See www.openkinetic.org for more project information
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
