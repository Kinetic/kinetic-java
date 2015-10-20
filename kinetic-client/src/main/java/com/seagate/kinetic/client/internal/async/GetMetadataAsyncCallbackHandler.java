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
