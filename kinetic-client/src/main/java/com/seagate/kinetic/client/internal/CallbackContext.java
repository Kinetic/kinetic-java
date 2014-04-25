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
