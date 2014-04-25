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
