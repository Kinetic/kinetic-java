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
package com.seagate.kinetic.example.client.async;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.KineticClient;

/**
 * Asynchronous get callback handler example.
 * 
 * @see CallbackHandler
 * @see KineticClient#getAsync(byte[], CallbackHandler)
 */
public class GetCallbackHandler implements CallbackHandler<Entry> {

	// calling instance/component
	private AsyncApiUsage master = null;

	public GetCallbackHandler(AsyncApiUsage master) {
		this.master = master;
	}

	@Override
	public void onSuccess(CallbackResult<Entry> result) {
		// inform master entry is received
		this.master.asyncGetReceived(result.getResult());
	}

	/**
	 * 
	 * @see AsyncKineticException
	 */
	@Override
	public void onError(AsyncKineticException exception) {
		// you may explore the API in AsyncKineticException
		System.out.println(exception.getRequestMessage());
	}

}
