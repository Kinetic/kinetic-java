/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.seagate.kinetic.simulator.performance;

import java.util.concurrent.CountDownLatch;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;

/**
 * 
 * Call back handler for performance test
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */
public class MyCallBackHandler implements CallbackHandler<Entry> {

	private CountDownLatch signal = null;

	public MyCallBackHandler(CountDownLatch signal) {
		this.signal = signal;
	}

	@Override
	public void onSuccess(CallbackResult<Entry> result) {
		signal.countDown();
	}

	@Override
	public void onError(AsyncKineticException exception) {
		throw new RuntimeException("call back error: " + exception.getMessage());
	}

}
