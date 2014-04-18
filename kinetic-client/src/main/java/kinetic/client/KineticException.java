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
 * Kinetic Client root Exception. All Kinetic client exceptions extend from this
 * class.
 * <p>
 * For each KineticClient synchronous operation, a KineticException or its
 * subclass instance is raised should the system encountered an error. For
 * example, version mismatch for a {@link KineticClient#put(Entry, byte[])}
 * operation. The root cause exception (if any) may be set in the
 * KineticException instance.
 * <p>
 * Applications may obtain the root cause exception through the
 * {@link KineticException#getCause()} API.
 * <p>
 * For each KineticClient asynchronous operation, a failed operation would cause
 * an AsyncCallbackException be generated and delivered to the registered
 * {@link CallbackHandler#onError(AsyncKineticException)} method.
 * 
 * @author Chiaming Yang
 * 
 * @see AsyncKineticException
 * @see KineticClient
 */
public class KineticException extends Exception {

	private static final long serialVersionUID = -649278492614600795L;

	/**
	 * Default constructor.
	 */
	public KineticException() {
		;
	}

	/**
	 * Construct a new instance of Kinetic exception with the specified message.
	 * 
	 * @param message
	 *            the message for the exception.
	 */
	public KineticException(String message) {
		super(message);
	}

	/**
	 * Construct a new instance of Kinetic exception with the specified root
	 * cause exception.
	 * 
	 * @param cause
	 *            the cause exception for this exception.
	 */
	public KineticException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 * Construct a new instance of Kinetic exception with the specified message
	 * and root cause exception.
	 * 
	 * @param message
	 *            the exception message.
	 * @param cause
	 *            the cause of the exception.
	 */
	public KineticException(String message, Throwable cause) {
		super(message, cause);
	}
}
