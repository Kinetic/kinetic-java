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
package com.seagate.kinetic.client.internal.async;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;

import com.seagate.kinetic.client.internal.CallbackContext;
import com.seagate.kinetic.common.lib.KineticMessage;

/**
 * Kinetic Asynchrounous call back handler.
 *
 * @author chiaming
 *
 * @param <T>
 */
public abstract class AsyncCallbackHandler<T> {

	public abstract AsyncKineticException checkAsyncResponseMessage(
			CallbackContext<T> context);

	public abstract CallbackResult<T> getCallbackResult(
			CallbackContext<T> context);

	public void onAsyncMessage(Object cbContext, KineticMessage response,
			AsyncKineticException exception) {

		@SuppressWarnings("unchecked")
		CallbackContext<T> context = (CallbackContext<T>) cbContext;

		context.setResponseMessage(response);

		CallbackHandler<T> handler = context.getCallbackHandler();

		if (exception == null) {
			exception = checkAsyncResponseMessage(context);
		}

		if (exception == null) {
			CallbackResult<T> result = getCallbackResult(context);
			handler.onSuccess(result);
		} else {
			handler.onError(exception);
		}

	}

}
