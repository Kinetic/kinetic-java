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
