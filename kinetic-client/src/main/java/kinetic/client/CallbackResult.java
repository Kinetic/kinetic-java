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
