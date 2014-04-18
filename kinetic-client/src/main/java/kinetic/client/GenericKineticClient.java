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
	 * Generic kinetic asynchronous request operation. This is a variation of
	 * the {@link #request(com.seagate.kinetic.proto.Kinetic.Message.Builder)}
	 * API.
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
