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
package com.seagate.kinetic.simulator.internal;

/**
 * @author James Hughes
 * @Refactor Chenchong(Emma) Li
 *
 */
public class KVStoreException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2646388022530610424L;

	public KVStoreException() {
		super();
	}

	public KVStoreException(String message) {
		super(message);
	}

}
