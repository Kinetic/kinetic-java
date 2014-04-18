// Do NOT modify or remove this copyright and confidentiality notice!
//
// Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
//
// The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
// Portions are also trade secret. Any use, duplication, derivation, distribution
// or disclosure of this code, for any reason, not expressly authorized is
// prohibited. All other rights are expressly reserved by Seagate Technology, LLC.

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
