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
package com.seagate.kinetic.usage.async;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;

/**
 * 
 * Callback handler Example
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */
public class PutAsyncCallbackHandler implements CallbackHandler<Entry>{
	
    private PutAsyncUsage putAsync = null;


	public PutAsyncCallbackHandler(PutAsyncUsage putAsync){
    	this.putAsync = putAsync;
    }
    
	@Override
	public void onSuccess(CallbackResult<Entry> result) {
		putAsync.received(result.getResult());
	}

	@Override
	public void onError(AsyncKineticException exception) {
        throw new RuntimeException(exception.getMessage());
	}

}
