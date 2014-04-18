/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.internal.async;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackResult;
import kinetic.client.EntryMetadata;

import com.seagate.kinetic.client.internal.CallbackContext;
import com.seagate.kinetic.client.internal.CallbackResultMessageFactory;
import com.seagate.kinetic.client.io.MessageHandler;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;

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
