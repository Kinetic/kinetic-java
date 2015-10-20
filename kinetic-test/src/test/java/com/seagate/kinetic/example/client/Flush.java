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
package com.seagate.kinetic.example.client;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;
import kinetic.client.advanced.AdvancedKineticClientFactory;
import kinetic.client.advanced.PersistOption;

/**
 * Kinetic Java client <code>flush</code> API usage example.
 */
public class Flush {

    public static void flush(String host, int port) throws KineticException,
            InterruptedException {

        // advancev kinetic client
        AdvancedKineticClient client = null;

        // Client configuration and initialization
        ClientConfiguration clientConfig = new ClientConfiguration();

        // create new client instance
        client = AdvancedKineticClientFactory
                .createAdvancedClientInstance(clientConfig);

        // entry
        Entry e = new Entry();

        for (int i = 0; i < 10; i++) {
            e.setKey(("hello-" + i).getBytes());
            e.setValue(("world-" + i).getBytes());
            client.putForced(e, PersistOption.ASYNC);
        }

        // flush data
        client.flush();

        // close kinetic client
        client.close();
    }

    /**
     * Ping a kinetic service and prints the rundtrip time.
     * 
     * @param args
     *            no used.
     * @throws KineticException
     *             if any errors occurred.
     * @throws InterruptedException
     *             if interrupted.
     */
    public static void main(String[] args) throws KineticException,
            InterruptedException {

        // default host/port
        String host = System.getProperty("kinetic.host", "localhost");
        String sport = System.getProperty("kinetic.port", "8123");
        int port = Integer.parseInt(sport);

        // over-ride the default host
        if (args.length > 0) {
            host = args[0];
        }

        // over-ride the default port
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        // flush data to db
        Flush.flush(host, port);
    }
}
