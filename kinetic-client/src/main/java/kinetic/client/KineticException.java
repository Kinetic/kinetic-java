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

import com.seagate.kinetic.common.lib.KineticMessage;

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
    
 // request message
    private KineticMessage request = null;

    // response message
    private KineticMessage response = null;


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
	
	/**
     * Get request message for this operation.
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
     * Get the response message from kinetic drive/simulator.
     *
     * @return The asynchronous response message from kinetic service
     */
    public KineticMessage getResponseMessage() {
        return this.response;
    }

    /**
     * Set the response message sent from kinetic drive/simulator.
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
