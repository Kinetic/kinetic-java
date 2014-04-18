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

import com.seagate.kinetic.common.lib.KineticMessage;

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

	// request message
	private KineticMessage request = null;

	// response message
	private KineticMessage response = null;

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

	/**
	 * Get request message for this asynchronous operation.
	 *
	 * @return request message for this asynchronous operation
	 */
	public KineticMessage getRequestMessage() {
		return this.request;
	}

	/**
	 *
	 * Set the kinetic request with the specified message.
	 *
	 * @param request
	 *            Kinetic request message.
	 */
	public void setRequestMessage(KineticMessage request) {
		this.request = request;
	}

	/**
	 * The asynchronous response message from kinetic drive/simulator.
	 *
	 * @return The asynchronous response message from kinetic service
	 */
	public KineticMessage getResponseMessage() {
		return this.response;
	}

	/**
	 * The asynchronous response message sent from kinetic drive/simulator.
	 * <p>
	 * called by the Kinetic Client Runtime.
	 *
	 * @param response
	 *            the response object from simulator.
	 */
	public void setResponseMessage(KineticMessage response) {
		this.response = response;
	}

}
