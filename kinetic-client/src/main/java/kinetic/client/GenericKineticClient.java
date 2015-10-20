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

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;

/**
 *
 * Generic kinetic raw/advanced interface. Applications may use this interface
 * to communicate with a Kinetic Service directly. Applications must know
 * details of protocol buffer usage for Kinetic in order to use this API
 * effectively.
 * <p>
 * In general, Applications should use the API provided in the
 * com.seagate.kinetic.client package for Kinetic operations. The boot-strap API
 * for the Kinetic client operation is
 * {@link KineticClientFactory#createInstance(ClientConfiguration)}.
 *
 * @see Message
 * @see KineticClientFactory
 * @see KineticClient
 */
public interface GenericKineticClient {

	/**
	 *
	 * Generic kinetic client raw/advanced request interface. Applications may
	 * use this interface to send a request to a Kinetic Service directly.
	 * Applications must know the details of Kinetic protocol buffer usage in
	 * order to use this API effectively.
	 * <p>
	 * In general, Applications should use the API provided in the
	 * com.seagate.kinetic.client package. The boot-strap API for the Kinetic
	 * client operation is
	 * {@link KineticClientFactory#createInstance(ClientConfiguration)}.
	 * <p>
	 *
	 * @param requestMessage
	 *            the request message to the Kinetic service.
	 *
	 * @return a respond message from the specified request.
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @see KineticMessage
	 * @see KineticClientFactory
	 * @see KineticClient
	 */
	public KineticMessage request(KineticMessage requestMessage)
			throws KineticException;

	/**
	 * Generic kinetic asynchronous request operation.
	 * <p>
	 *
	 * @param requestMessage
	 *            the request message to the kinetic service.
	 *
	 * @param callback
	 *            Kinetic client runtime invokes the CallbackHandler.onSuccess()
	 *            when a successful request's response message is available.
	 *            CallbackHandler.onError(KineticException e) is invoked if any
	 *            internal error occurs.
	 * @param <T>
	 *            The type must be consistent with the corresponding synchronous
	 *            request method's return type. For example, for an asynchronous
	 *            get request, the parameter specified must be an
	 *            <code>Entry</code> type.
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 * @see #request(KineticMessage)
	 * @see CallbackHandler
	 * @see CallbackResult
	 */
	public <T> void requestAsync(KineticMessage requestMessage,
			CallbackHandler<T> callback) throws KineticException;
}
