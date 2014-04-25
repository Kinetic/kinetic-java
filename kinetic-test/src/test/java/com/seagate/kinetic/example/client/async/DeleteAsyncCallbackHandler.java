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
