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
