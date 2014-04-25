/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
