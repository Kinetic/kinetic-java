/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
