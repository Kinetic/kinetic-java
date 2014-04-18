/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
