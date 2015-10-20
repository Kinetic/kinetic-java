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
 *
 * The exception instance that delivers to an asynchronous kinetic request if an
 * error occurred during the asynchronous request.
 * <p>
 * When an application calls a KineticClient asynchronous operation, the result
 * is delivered to the registered callback handler. Kinetic client runtime
 * library calls the {@link CallbackHandler#onSuccess(CallbackResult)} for each
 * successful asynchronous operation.
 * <p>
 * Should an error occurred, such as Version MisMatch for a put() operation, the
 * raised exception is delivered to the registered CallbackHandler. Kinetic
 * client runtime library calls the
 * {@link CallbackHandler#onError(AsyncKineticException)} for each failed
 * asynchronous operation.
 *
 * @author chiaming
 *
 * @see KineticClient
 * @see CallbackHandler
 */
public class AsyncKineticException extends KineticException {

	private static final long serialVersionUID = 2296887193756788515L;

	/**
	 * Default constructor.
	 */
	public AsyncKineticException() {
		super();
	}

	/**
	 * Construct a new instance of this Exception with the specified message.
	 *
	 * @param message
	 *            the message for the exception.
	 */
	public AsyncKineticException(String message) {
		super(message);
	}

	/**
	 * Construct a new instance of AsyncKineticException with the specified
	 * root cause exception.
	 *
	 * @param cause
	 *            the cause exception for this exception.
	 */
	public AsyncKineticException(Throwable cause) {
		super(cause);
	}

	/**
	 *
	 * Construct a new instance of AsyncKineticException with the specified
	 * message and root cause exception.
	 *
	 * @param message
	 *            the exception message.
	 * @param cause
	 *            the cause of the exception.
	 */
	public AsyncKineticException(String message, Throwable cause) {
		super(message, cause);
	}

}
