package com.seagate.kinetic.example.batchop;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;

public class DeleteCbHandler implements CallbackHandler<Boolean> {

    @Override
    public void onSuccess(CallbackResult<Boolean> result) {
        System.out.println("*** delete cb on success invoked");
    }

    @Override
    public void onError(AsyncKineticException exception) {
        exception.printStackTrace();
    }

}
