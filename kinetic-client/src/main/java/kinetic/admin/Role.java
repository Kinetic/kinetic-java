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
package kinetic.admin;

/**
 * 
 * Kinetic drive access control roles.
 * 
 * @see Domain
 * @see ACL
 */
public enum Role {
	/**
	 * <pre>
	 * can read key/values
	 * </pre>
	 */
	READ,
	/**
	 * <pre>
	 * can write key/values
	 * </pre>
	 */
	WRITE,
	/**
	 * <pre>
	 * can delete key/values
	 * </pre>
	 */
	DELETE,
	/**
	 * <pre>
	 * can do a range
	 * </pre>
	 */
	RANGE,
	/**
	 * <pre>
	 * can set up and a device
	 * </pre>
	 */
	SETUP,
	/**
	 * <pre>
	 * can do a peer to peer operation
	 * </pre>
	 */
	P2POP,
	/**
	 * <pre>
	 * can get log
	 * </pre>
	 */
	GETLOG,
	/**
	 * <pre>
	 * can set up the security roles of the device
	 * </pre>
	 */
	SECURITY;
}
