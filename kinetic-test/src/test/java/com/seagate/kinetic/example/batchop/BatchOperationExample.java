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
package com.seagate.kinetic.example.batchop;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.AsyncKineticException;
import kinetic.client.BatchOperation;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

/**
 * Kinetic client batch operation usage example.
 * 
 * @author chiaming
 *
 */
public class BatchOperationExample implements CallbackHandler<Entry> {

    private final static java.util.logging.Logger logger = Logger
            .getLogger(BatchOperationExample.class.getName());

    public void run(String host, int port) throws KineticException,
            UnsupportedEncodingException {

        // kinetic client
        KineticClient client = null;

        // Client configuration and initialization
        ClientConfiguration clientConfig = new ClientConfiguration();

        clientConfig.setHost(host);
        clientConfig.setPort(port);

        // create client instance
        client = KineticClientFactory.createInstance(clientConfig);

        // put entry bar
        Entry bar = new Entry();
        bar.setKey("bar".getBytes("UTF8"));
        bar.setValue("bar".getBytes("UTF8"));
        bar.getEntryMetadata().setVersion("1234".getBytes("UTF8"));

        client.putForced(bar);

        logger.info("*** starting batch operation ...");

        // start batch a new batch operation
        BatchOperation batch = client.createBatchOperation();

        // put foo
        Entry foo = new Entry();
        foo.setKey("foo".getBytes("UTF8"));
        foo.setValue("foo".getBytes("UTF8"));
        foo.getEntryMetadata().setVersion("5678".getBytes("UTF8"));

        batch.putForcedAsync(foo, this);

        // delete bar
        DeleteCbHandler dhandler = new DeleteCbHandler();
        batch.deleteAsync(bar, dhandler);

        // end/commit batch operation
        batch.commit();

        logger.info("*** batch op committed ...");

        // start verifying result

        // get foo, expect to find it
        Entry foo1 = client.get(foo.getKey());

        // cannot be null
        if (foo1 == null) {
            throw new RuntimeException("Expect to find foo but not found");
        }

        byte[] key3 = foo1.getKey();
        String k = new String(key3, "UTF8");

        byte[] value3 = foo1.getValue();
        String v = new String(value3, "UTF8");

        logger.info("expect foo existed, key =" + k + ", value = "
                + v);

        // get entry, expect to be not found
        Entry bar1 = client.get(bar.getKey());
        if (bar1 != null) {
            throw new RuntimeException("error: found deleted entry.");
        }

        logger.info("Expect entry bar to be null, entry=" + bar1);

        // close kinetic client
        client.close();
    }

    @Override
    public void onSuccess(CallbackResult<Entry> result) {
        logger.info("put callback result received ...");
    }

    @Override
    public void onError(AsyncKineticException exception) {
        logger.log(Level.WARNING, exception.getMessage(), exception);
    }

    public static void main(String[] args) throws KineticException,
            InterruptedException, UnsupportedEncodingException {

        BatchOperationExample batch = new BatchOperationExample();

        batch.run("localhost", 8123);
    }

}
