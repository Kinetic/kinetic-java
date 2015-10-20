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
