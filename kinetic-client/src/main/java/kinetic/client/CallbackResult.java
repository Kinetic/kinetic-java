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

/**
 * Asynchronous callback result interface.
 * <p>
 * When applications call a KineticClient asynchronous operation, the result is
 * delivered to the registered callback handler. Kinetic client runtime library
 * calls the {@link CallbackHandler#onSuccess(CallbackResult)} for each
 * successful asynchronous operation.
 * <p>
 * The Param Type matches the corresponding return type for its synchronous
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
	 * The Param Type matches the corresponding return type for its
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
