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
package com.seagate.kinetic.client.internal.util.bigobject;

import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;

public class GetxCallbackHandler implements
CallbackHandler<Entry> {

	private final Logger logger = Logger
			.getLogger(GetxCallbackHandler.class.getName());

	private long counter = 0;

	private DataOutputStream dos = null;

	private long total = 0;

	public GetxCallbackHandler(DataOutputStream dos) {
		this.dos = dos;
	}

	@Override
	public void onSuccess(CallbackResult<Entry> result) {
		try {

			byte[] value = result.getResult().getValue();

			dos.write(value);

			total += value.length;

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			this.decreaseCounter();
		}
	}

	@Override
	public void onError(AsyncKineticException exception) {
		logger.log(Level.WARNING, exception.getMessage(), exception);
	}

	public synchronized void waitForFinish() {
		while (this.counter > 0) {
			try {
				this.wait(500);

				if (this.counter > 0) {
					System.out.println("waiting for finish, counter="
							+ this.counter);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized void increaseCounter() {
		this.counter++;
	}

	public synchronized void decreaseCounter() {
		this.counter--;

		if (this.counter == 0) {
			this.notifyAll();
		}
	}

	public long getTotalRead() {
		return this.total;
	}

}
