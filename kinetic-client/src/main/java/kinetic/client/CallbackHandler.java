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
package kinetic.client;


/**
 * Kinetic asynchronous callback handler interface.
 * <p>
 * Applications implement this interface and pass an instance of the
 * implementation for each asynchronous request.
 */
public interface CallbackHandler<T> {

	/**
	 * This method is invoked for each successful asynchronous operation.
	 * <p>
	 * The CallbackHandler
	 * {@link #onError(kinetic.client.AsyncKineticException)} method
	 * is invoked if an error occurred.
	 * 
	 * @param result
	 *            the callback result for a specific asynchronous operation.
	 */
	public void onSuccess(CallbackResult<T> result);

	/**
	 * This method is called by KineticClient client runtime when there is an error
	 * occurred for an asynchronous request.
	 * 
	 * @param exception
	 *            the exception instance associated with the error.
	 */
	public void onError(AsyncKineticException exception);
}
