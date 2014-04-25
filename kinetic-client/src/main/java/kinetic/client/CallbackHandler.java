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
