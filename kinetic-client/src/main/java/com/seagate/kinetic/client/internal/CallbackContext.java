/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.internal;

import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;

import com.seagate.kinetic.common.lib.KineticMessage;

/**
 * Java API asynchronous callback context.
 *
 * @author chiaming
 *
 * @param <T>
 *            callback context type.
 *
 * @see CallbackHandler
 * @see CallbackResult
 * @see KineticMessage
 */
public class CallbackContext<T> {

	// callback handler
	private CallbackHandler<T> handler = null;

	// callback result
	private CallbackResult<T> result = null;

	// request message
	private KineticMessage request = null;

	// response message
	private KineticMessage response = null;

	/**
	 * Construct a callback context with the specified callback handler.
	 *
	 * @param handler
	 *            the callback handler for the callback context.
	 */
	public CallbackContext(CallbackHandler<T> handler) {
		this.handler = handler;
	}

	/**
	 * Set request message associated with the asynchronous callback context.
	 *
	 * @param request
	 *            request message associated with the asynchronous callback
	 *            context
	 */
	public void setRequestMessage(KineticMessage request) {
		this.request = request;
	}

	/**
	 * Set response message associated with the asynchronous callback context.
	 *
	 * @param response
	 *            response message associated with the asynchronous callback
	 *            context
	 */
	public void setResponseMessage(KineticMessage response) {
		this.response = response;
	}

	/**
	 * Get the request message.
	 *
	 * @return the request message
	 */
	public KineticMessage getRequestMessage() {
		return this.request;
	}

	/**
	 * Get the response message.
	 *
	 * @return the response message
	 */
	public KineticMessage getResponseMessage() {
		return this.response;
	}

	/**
	 * Get callback handler.
	 *
	 * @return the callback handler.
	 */
	public CallbackHandler<T> getCallbackHandler() {
		return this.handler;
	}

	/**
	 * Set callback result.
	 *
	 * @param result
	 *            callback result
	 */
	public void setCallbackResult(CallbackResult<T> result) {
		this.result = result;
	}

	/**
	 * Get callback result.
	 *
	 * @return callback result.
	 */
	public CallbackResult<T> getCallbackResult() {
		return this.result;
	}

}
