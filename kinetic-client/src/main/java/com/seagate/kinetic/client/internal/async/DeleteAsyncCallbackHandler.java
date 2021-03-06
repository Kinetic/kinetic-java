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

import com.seagate.kinetic.client.internal.CallbackContext;
import com.seagate.kinetic.client.internal.CallbackResultMessageFactory;
import com.seagate.kinetic.client.io.MessageHandler;

public class DeleteAsyncCallbackHandler extends AsyncCallbackHandler<Boolean> {

	@Override
	public AsyncKineticException checkAsyncResponseMessage(
			CallbackContext<Boolean> context) {

		return MessageHandler.checkDeleteReply(context);
	}

	@Override
	public CallbackResult<Boolean> getCallbackResult(
			CallbackContext<Boolean> context) {

		return CallbackResultMessageFactory
				.createDeleteCallbackResultMessage(context);
	}

}
