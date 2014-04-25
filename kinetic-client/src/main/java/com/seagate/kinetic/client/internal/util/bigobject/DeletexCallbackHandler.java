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

/**
 * 
 * @author chiaming
 * 
 */
public class DeletexCallbackHandler implements
CallbackHandler<Boolean> {

	private final Logger logger = Logger
			.getLogger(DeletexCallbackHandler.class.getName());

	private long opCount = 0;

	private long deletedCount = 0;

	public DeletexCallbackHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onSuccess(CallbackResult<Boolean> result) {

		synchronized (this) {

			if (result.getResult().booleanValue() == true) {
				this.deletedCount++;
			}

			this.opCount--;

			logger.fine("deleted count= " + this.deletedCount + ", opCount="
					+ this.opCount);

			if (this.opCount == 0) {
				this.notifyAll();
			}
		}
	}

	@Override
	public void onError(AsyncKineticException exception) {
		logger.log(Level.WARNING, exception.getMessage(), exception);
	}

	public synchronized void increaseCount() {
		this.opCount++;
	}

	public synchronized long waitForFinish() {
		while (this.opCount > 0) {
			try {
				this.wait(500);

				if (this.opCount > 0) {
					System.out.println("waiting for finish, counter="
							+ this.opCount + ", deleted count="
							+ this.deletedCount);
				}
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}

		// System.out.println("total deleted count=" + this.deletedCount);

		return this.deletedCount;
	}
}
