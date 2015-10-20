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
 * 
 * Asynchronous delete callback handler example.
 * 
 * @see AsyncApiUsage
 * @see CallbackHandler
 * @see KineticClient#deleteAsync(Entry, CallbackHandler)
 * @see KineticClient#deleteForcedAsync(byte[], CallbackHandler)
 */
public class DeleteAsyncCallbackHandler implements CallbackHandler<Boolean> {

	private AsyncApiUsage master = null;

	public DeleteAsyncCallbackHandler(AsyncApiUsage master) {
		this.master = master;
	}

	@Override
	public void onSuccess(CallbackResult<Boolean> result) {
		// notify master entry deleted
		this.master.asyncDeletedInStore(result.getRequestKey());
	}

	@Override
	public void onError(AsyncKineticException exception) {
		// print stack trace
		exception.printStackTrace();

		// print request message
		System.out.println("request message: " + exception.getRequestMessage());
	}

}
