package com.seagate.kinetic.simulator.client.async;

import java.util.concurrent.LinkedBlockingQueue;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;

public class MyCallBack implements CallbackHandler<Entry> {

	private LinkedBlockingQueue<KineticMessage> pbq = null;

	private KineticMessage request = null;

	public MyCallBack(LinkedBlockingQueue<KineticMessage> pbq, KineticMessage m) {
		this.pbq = pbq;
		request = m;
	}

	public void onMessage(KineticMessage message) {

		if (message.getMessage().getCommand().getStatus().getCode() != StatusCode.SUCCESS) {
			throw new RuntimeException("unsuccessful status, message="
					+ message.getMessage().getCommand().getStatus()
							.getStatusMessage() + ", code="
					+ message.getMessage().getCommand().getStatus().getCode());
		}

		if (this.request.getMessage().getCommand().getHeader().getSequence() != message
				.getMessage().getCommand()
				.getHeader().getAckSequence()) {
			throw new RuntimeException("call back sequence error");
		}

		this.pbq.add(message);
		// System.out.println("received message: " + message);
	}

	@Override
	public void onSuccess(CallbackResult<Entry> result) {

		// System.out.println("received async callback, result="
		// + result.getResult().getClass().getName());
		this.pbq.add(result.getResponseMessage());
	}

	@Override
	public void onError(AsyncKineticException exception) {
		// TODO Auto-generated method stub

	}

}
