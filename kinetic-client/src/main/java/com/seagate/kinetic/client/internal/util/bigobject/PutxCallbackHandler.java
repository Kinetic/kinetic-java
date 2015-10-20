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

import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;

public class PutxCallbackHandler implements
CallbackHandler<Entry> {

	private final Logger logger = Logger
			.getLogger(PutxCallbackHandler.class.getName());

	private long counter = 0;

	public PutxCallbackHandler() {
		;
	}

	@Override
	public void onSuccess(CallbackResult<Entry> result) {
		this.decreaseCounter();
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
				logger.log(Level.WARNING, e.getMessage(), e);
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

}
