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
package com.seagate.kinetic;

/**
 * 
 * Generate key/value pair.
 * 
 */
public class KVGenerator {
	private final int alignLength = ("" + Integer.MAX_VALUE).length() + 1;
	private String keyPrefix = "key";
	private String valuePrefix = "value";
	private int start = 0;
	private int current = 0;

	/**
	 * 
	 * Generate key.
	 * 
	 * @param id
	 *            key's number
	 * @return string
	 *            a string of key
	 */
	private String align(int id) {
		String idAsString = "" + id;
		int length = idAsString.length();
		for (int i = 0; i < alignLength - length; i++) {
			idAsString = "0" + idAsString;
		}
		return idAsString;
	}

	/**
	 * 
	 * Reset the current location to start.
	 * 
	 */
	public void reset() {
		this.current = start;
	}

	/**
	 * 
	 * Get next key.
	 * 
	 * @return string
	 *            a string of key
	 */
	public synchronized String getNextKey() {
		if (current >= Integer.MAX_VALUE) {
			throw new RuntimeException("out of keys");
		}
		return keyPrefix + align(current++);
	}

	/**
	 * 
	 * Generate value.
	 * 
	 * @param key
	 *            a string key
	 * @return string
	 *            a string of value
	 */
	public String getValue(String key) {
		int keyId = Integer.parseInt(key.replaceAll(keyPrefix, ""));
		return valuePrefix + keyId;
	}
}
