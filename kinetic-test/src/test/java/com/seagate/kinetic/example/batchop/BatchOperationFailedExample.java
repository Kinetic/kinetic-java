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
package com.seagate.kinetic.example.batchop;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.BatchAbortedException;
import kinetic.client.BatchOperation;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.seagate.kinetic.proto.Kinetic.Command.Status;

/**
 * Kinetic client batch operation usage example.
 * 
 * @author chiaming
 *
 */
public class BatchOperationFailedExample {

    private final static java.util.logging.Logger logger = Logger
            .getLogger(BatchOperationFailedExample.class.getName());

    public void run(String host, int port) throws KineticException,
            UnsupportedEncodingException {

        // kinetic client
        KineticClient client = null;

        try {

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

            // put bar
            client.putForced(bar);

            // delete foo
            client.deleteForced("foo".getBytes("UTF8"));

            logger.info("*** starting batch operation ...");

            // start batch a new batch operation
            BatchOperation batch = client.createBatchOperation();

            // put foo
            Entry foo = new Entry();
            foo.setKey("foo".getBytes("UTF8"));
            foo.setValue("foo".getBytes("UTF8"));
            foo.getEntryMetadata().setVersion("5678".getBytes("UTF8"));

            batch.putForced(foo);

            // put bar with wrong version, will fail
            bar.getEntryMetadata().setVersion("12341234".getBytes("UTF8"));
            batch.put(bar, "".getBytes());

            // end/commit batch operation
            try {
                batch.commit();
            } catch (BatchAbortedException e) {
                // get status
                Status status = e.getResponseMessage().getCommand().getStatus();

                int index = e.getFailedOperationIndex();

                logger.info("received expected exception: " + status.getCode()
                        + ":" + status.getStatusMessage() + ", index=" + index);
            }

            Entry foo1 = client.get("foo".getBytes("UTF8"));
            if (foo1 != null) {
                throw new RuntimeException(
                        "received unexpected value from key foo");
            } else {
                logger.info("Test was successfully validated.");
            }

        } catch (KineticException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            client.close();
        }
    }

    public static void main(String[] args) throws KineticException,
            InterruptedException, UnsupportedEncodingException {

        BatchOperationFailedExample batch = new BatchOperationFailedExample();

        batch.run("localhost", 8123);
    }

}
