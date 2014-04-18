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
 * Asynchronous callback result interface.
 * <p>
 * When applications call a KineticClient asynchronous operation, the result is
 * delivered to the registered callback handler. Kinetic client runtime library
 * calls the {@link CallbackHandler#onSuccess(CallbackResult)} for each
 * successful asynchronous operation.
 * <p>
 * The Param Type <T> matches the corresponding return type for its synchronous
 * API variation. For example, a {@link KineticClient#putForced(Entry)} operation
 * would have a CallbackResult of type {@link Entry}.
 *
 * @see CallbackHandler#onSuccess(CallbackResult)
 *
 * @see KineticClient
 */
public interface CallbackResult<T> {

	/**
	 * Get the request message associated with the asynchronous request.
	 *
	 * @return request message.
	 */
	public KineticMessage getRequestMessage();

	/**
	 * Get the respond message associated with the asynchronous request.
	 *
	 * @return the response message associated with the asynchronous request.
	 */
	public KineticMessage getResponseMessage();

	/**
	 * Get callback result.
	 * <p>
	 * The Param Type <T> matches the corresponding return type for its
	 * synchronous API variation. For example, a
	 * {@link KineticClient#putForced(Entry)} operation would have a
	 * CallbackResult of type {@link Entry}.
	 *
	 * @return the result instance corresponding to the asynchronous request.
	 */
	public T getResult();

	/**
	 * Get the key field of the key/value asynchronous operation request
	 * message.
	 * <p>
	 *
	 * @return the key field of the request message.
	 */
	public byte[] getRequestKey();

}
