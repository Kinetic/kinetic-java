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
