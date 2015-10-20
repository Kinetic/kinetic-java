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

import kinetic.client.BatchOperation;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

/**
 * Kinetic client batch operation usage example.
 * <p>
 * This example shows how to use the batch operation API to abort a batch
 * operation.
 * 
 * @author chiaming
 * @see BatchOperation#abort()
 */
public class BatchOperationAbortExample {

    public void run(String host, int port) throws KineticException,
            UnsupportedEncodingException {

        // kinetic client
        KineticClient client = null;

        // Client configuration and initialization
        ClientConfiguration clientConfig = new ClientConfiguration();

        clientConfig.setHost(host);
        clientConfig.setUseSsl(true);
        clientConfig.setPort(8443);
        // clientConfig.setPort(port);

        // create client instance
        client = KineticClientFactory.createInstance(clientConfig);

        // put entry bar
        Entry bar = new Entry();
        bar.setKey("bar".getBytes("UTF8"));
        bar.setValue("bar".getBytes("UTF8"));
        bar.getEntryMetadata().setVersion("1234".getBytes("UTF8"));

        client.putForced(bar);

        // clean up before batch op
        client.deleteForced("foo".getBytes("UTF8"));

        // start batch a new batch operation
        BatchOperation batch = client.createBatchOperation();

        // put foo
        Entry foo = new Entry();
        foo.setKey("foo".getBytes("UTF8"));
        foo.setValue("foo".getBytes("UTF8"));
        foo.getEntryMetadata().setVersion("5678".getBytes("UTF8"));

        batch.putForced(foo);

        // delete bar
        batch.delete(bar);

        // end/commit batch operation
        batch.abort();

        // start verifying result

        // get foo, expect to find it
        Entry foo1 = client.get(foo.getKey());

        // must be null
        if (foo1 != null) {
            throw new RuntimeException("Does not expect to find foo");
        }

        // get entry, expect to be found
        Entry bar1 = client.get(bar.getKey());
        if (bar1 == null) {
            throw new RuntimeException("error: cannot find bar entry.");
        }

        System.out.println("Verification passed.");

        // close kinetic client
        client.close();
    }

    public static void main(String[] args) throws KineticException,
            InterruptedException, UnsupportedEncodingException {

        BatchOperationAbortExample batch = new BatchOperationAbortExample();

        batch.run("localhost", 8123);
    }

}
