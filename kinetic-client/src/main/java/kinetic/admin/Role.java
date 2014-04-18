/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
