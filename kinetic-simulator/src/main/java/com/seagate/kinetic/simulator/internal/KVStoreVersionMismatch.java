/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.internal;


/**
 * @author James Hughes
 * @Refactor Chenchong(Emma) Li
 */
public class KVStoreVersionMismatch extends KVStoreException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2872977992697973315L;
	
	public KVStoreVersionMismatch(String message) {
		super(message);
	}


}
