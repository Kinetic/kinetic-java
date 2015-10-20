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

import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.KineticClient;

/**
 * 
 * Put async callback handler example.
 * 
 * @see AsyncApiUsage
 * @see CallbackHandler
 * @see KineticClient#putForcedAsync(Entry, CallbackHandler)
 * @see KineticClient#putAsync(Entry, byte[], CallbackHandler)
 */
public class PutAsyncCallbackHandler implements CallbackHandler<Entry> {

	private final static java.util.logging.Logger logger = Logger
			.getLogger(PutAsyncCallbackHandler.class.getName());

	private AsyncApiUsage master = null;

	public PutAsyncCallbackHandler(AsyncApiUsage master) {
		this.master = master;
	}

	@Override
	public void onSuccess(CallbackResult<Entry> result) {

		// inform master that entry has persisted in store
		this.master.asyncPutInStore(result.getResult());
	}

	@Override
	public void onError(AsyncKineticException exception) {

		// log exception
		logger.log(Level.WARNING, exception.getMessage(), exception);
	}

}
