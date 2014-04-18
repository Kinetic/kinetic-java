/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
