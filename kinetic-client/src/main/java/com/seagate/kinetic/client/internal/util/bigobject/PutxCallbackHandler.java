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
