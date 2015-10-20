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
