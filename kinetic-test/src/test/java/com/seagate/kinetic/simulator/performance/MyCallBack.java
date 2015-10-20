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
package com.seagate.kinetic.simulator.performance;

import java.util.concurrent.LinkedBlockingQueue;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

public class MyCallBack implements CallbackHandler<Entry> {

	private LinkedBlockingQueue<KineticMessage> pbq = null;

	private KineticMessage request = null;

	public MyCallBack(LinkedBlockingQueue<KineticMessage> pbq, KineticMessage m) {
		this.pbq = pbq;
		request = m;
	}

	// @Override
	public void onMessage(KineticMessage message) {

		if (message.getCommand().getStatus().getCode() != StatusCode.SUCCESS) {
			throw new RuntimeException("unsuccessful status, message="
					+ message.getCommand().getStatus()
					.getStatusMessage() + ", code="
					+ message.getCommand().getStatus().getCode());
		}

		if (this.request.getCommand().getHeader().getSequence() != message
				.getCommand().getHeader().getAckSequence()) {
			throw new RuntimeException("call back sequence error");
		}

		this.pbq.add(message);
		// System.out.println("received message: " + message);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onSuccess(CallbackResult result) {
		// TODO Auto-generated method stub
		this.pbq.add(result.getResponseMessage());
	}

	@Override
	public void onError(AsyncKineticException exception) {
		// TODO Auto-generated method stub

	}

}
