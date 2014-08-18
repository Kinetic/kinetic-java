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
package com.seagate.kinetic.client.internal.async;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackResult;
import kinetic.client.EntryMetadata;

import com.seagate.kinetic.client.internal.CallbackContext;
import com.seagate.kinetic.client.internal.CallbackResultMessageFactory;
import com.seagate.kinetic.client.io.MessageHandler;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;

public class GetMetadataAsyncCallbackHandler extends
		AsyncCallbackHandler<EntryMetadata> {

	private MessageType mtype = null;

	public GetMetadataAsyncCallbackHandler(MessageType mtype) {
		this.mtype = mtype;
	}

	@Override
	public CallbackResult<EntryMetadata> getCallbackResult(
			CallbackContext<EntryMetadata> context) {

		return CallbackResultMessageFactory
				.createGetMetadataCallbackResultMessage(context);
	}

	@Override
	public AsyncKineticException checkAsyncResponseMessage(
			CallbackContext<EntryMetadata> context) {
		return MessageHandler.checkGetMetadataReply(context, mtype);
	}
}
