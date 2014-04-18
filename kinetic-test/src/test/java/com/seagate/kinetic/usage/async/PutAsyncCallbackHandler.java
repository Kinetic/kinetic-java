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
