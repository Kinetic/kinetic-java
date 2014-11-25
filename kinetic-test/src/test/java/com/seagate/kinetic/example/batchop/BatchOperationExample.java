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

        logger.info("*** starting batch op ...");

        byte[] key = "hello".getBytes("UTF8");

        // start batch a new batch operation
        BatchOperation batch = client.createBatchOperation();

        // put entry 1
        Entry entry = new Entry();
        entry.setKey(key);
        entry.setValue("world".getBytes("UTF8"));

        // client.putAsync(entry, null, this);
        batch.putForcedAsync(entry, this);

        // put entry 2
        Entry entry2 = new Entry();
        byte[] key2 = "hello2".getBytes("UTF8");
        entry2.setKey(key2);
        entry2.setValue("world2".getBytes("UTF8"));

        // client.putAsync(entry2, null, this);
        batch.putForcedAsync(entry2, this);

        // delet entry 1
        DeleteCbHandler dhandler = new DeleteCbHandler();
        // client.deleteAsync(entry, dhandler);
        batch.deleteAsync(entry, dhandler);

        // end/commit batch operation
        batch.commit();

        logger.info("*** batch op committed ...");

        // start verifying result

        // get entry2, expect to find it
        Entry entry3 = client.get(key2);

        byte[] key3 = entry3.getKey();
        String k = new String(key3, "UTF8");

        byte[] value3 = entry3.getValue();
        String v = new String(value3, "UTF8");

        logger.info("expect entry2 existed, key =" + k + ", value = "
                + v);

        // get entry, expect to be not found
        Entry entry4 = client.get(key);
        if (entry4 != null) {
            throw new RuntimeException("error: found deleted entry ...");
        }

        logger.info("Expect entry hello to be null, entry=" + entry4);

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
