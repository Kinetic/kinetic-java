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

import kinetic.client.CallbackResult;

import com.seagate.kinetic.common.lib.KineticMessage;

/**
 * Java API Asynchronous callback result implementation.
 *
 * @author chiaming yang
 *
 * @param <T>
 *            callback result type.
 */
public class AsyncCallbackResult<T> implements CallbackResult<T> {

	// request message
	private KineticMessage request = null;

	// response message
	private KineticMessage response = null;

	// callback result
	private T result;

	/**
	 * default constructor.
	 */
	public AsyncCallbackResult() {
		;
	}

	/**
	 * Construct a new instance of asynchronous call back result with the
	 * specified parameters.
	 *
	 * @param request
	 *            the associated request object.
	 * @param response
	 *            the associated response object
	 * @param result
	 *            the associated call back result.
	 */
	public AsyncCallbackResult(KineticMessage request, KineticMessage response,
			T result) {
		this.request = request;
		this.response = response;
		this.result = result;
	}

	/**
	 * Set request instance.
	 *
	 * @param request
	 *            the request object for the asynchronous operation.
	 */
	public void setRequestMessage(KineticMessage request) {
		this.request = request;
	}

	/**
	 * Set response object.
	 *
	 * @param response
	 *            the response object for the asynchronous operation.
	 */
	public void setResponseMessage(KineticMessage response) {
		this.response = response;
	}

	/**
	 * Set the result object.
	 *
	 * @param result
	 *            the result object for the asynchronous operation.
	 */
	public void setResult(T result) {
		this.result = result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KineticMessage getRequestMessage() {
		return this.request;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KineticMessage getResponseMessage() {
		return this.response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getResult() {
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getRequestKey() {

		// check if request message is present
		if (this.request == null) {
			throw new NullPointerException("request message cannot be null.");
		}

		// return the key field of the request message.
		return this.request.getCommand().getBody().getKeyValue()
				.getKey()
				.toByteArray();
	}

}
