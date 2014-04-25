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

package com.seagate.kinetic.simulator.persist;

import com.google.protobuf.ByteString;

public class KVKey implements Comparable<KVKey>  {

	private final byte[] k;

	public KVKey(ByteString k) {
		this(k.toByteArray());
	}

	public KVKey(byte[] k) {
		this.k = k;
	}

	public byte[] getKey() {
		return k;
	}

	public ByteString toByteString() {
		return ByteString.copyFrom(k);
	}


	// THis compares two keys. If they are the same size then each byte is
	// compared. The first mis-compare decides who is higher or lower. If
	// the length is different then the common base is compared as before
	// and then the longer of the two arrays are higher (regardless of what
	// they contain). This will honor the k1.equals(k2) requirement.

	@Override
	public int compareTo(KVKey k2) {

		if (k == null)
			throw new NullPointerException();

		byte[] b1 = getKey();
		byte[] b2 = k2.getKey();

		int l1 = b1.length;
		int l2 = b2.length;

		int l = Math.min(l1, l2);

		for (int i = 0; i < l; i++) {
			if ((b1[i] & 0xff) < (b2[i] & 0xff))
				return -1;
			if ((b1[i] & 0xff) > (b2[i] & 0xff))
				return 1;
		}
		// the roots are the same.
		if (l1 == l2) {
			//			if (!Arrays.equals(b1, b2)) {
			//				LOG.fine("b1: " + Hmac.toString(b1));
			//				LOG.fine("b2: " + Hmac.toString(b2));
			//				throw new Error("KVKey CompareTo broken");
			//			}
			return 0;
		}

		// Which ever is longer is higher.
		if (l1 < l2)
			return -1;
		return 1;
	}

}
