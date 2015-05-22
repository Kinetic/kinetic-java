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

import kinetic.client.BatchAbortedException;
import kinetic.client.BatchOperation;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

/**
 * Kinetic client batch operation usage example.
 * <p>
 * This example shows a version mismatch example that caused a batch commit to
 * fail.
 * <p>
 * @author chiaming
 */
public class BatchOperationVersionMismatchExample {

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

        try {
            // start batch a new batch operation
            BatchOperation batch = client.createBatchOperation();

            // put foo
            Entry foo = new Entry();
            foo.setKey("foo".getBytes("UTF8"));
            foo.setValue("foo".getBytes("UTF8"));

            // put foo
            batch.putForced(foo);

            // set bar entry with wrong version
            bar.getEntryMetadata().setVersion("4321".getBytes("UTF8"));

            // put bar in batch, this will cause the commit to fail
            batch.put(bar, "1".getBytes());

            // end/commit batch operation
            batch.commit();

            throw new RuntimeException("Expected exception was not received");

        } catch (BatchAbortedException abe) {
            System.out.println("Received expected exception, reason:  "
                    + abe.getMessage());
            System.out.println("Verification passed.");
        } finally {
            // close kinetic client
            client.close();
        }
    }

    public static void main(String[] args) throws KineticException,
            InterruptedException, UnsupportedEncodingException {

        BatchOperationVersionMismatchExample batch = new BatchOperationVersionMismatchExample();

        batch.run("localhost", 8123);
    }

}
