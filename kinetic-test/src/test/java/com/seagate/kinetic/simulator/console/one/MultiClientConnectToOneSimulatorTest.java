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
package com.seagate.kinetic.simulator.console.one;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;

import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;

@Test(groups = {"simulator"})
public class MultiClientConnectToOneSimulatorTest extends IntegrationTestCase {
    // This number should be changed when real test, now the maximum number is
    // 10,000 if simulator and client run in two jvm.
    // the maximum number is 5,000 if simulator and client run in the same jvm.
    private static int CLIENT_COUNT = 100;
    private String key = "hello";
    private String value = "world";
    private String newVersion = "0";

    private List<KineticClient> clients;

    @BeforeMethod
    public void setUp() throws Exception {
        clients = new ArrayList<KineticClient>();
        for (int i = 0; i < CLIENT_COUNT; i++) {
            KineticClient client = KineticClientFactory
                    .createInstance(getClientConfig());
            clients.add(client);
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {
        for (KineticClient client : clients) {
            client.close();
        }
    }

    @Test
    public void multiClientConnectToOneSimulatorTest() throws KineticException {
        for (KineticClient client : clients) {
            operation(client);
        }
    }

    private void operation(KineticClient client) throws KineticException {
        Entry entry = new Entry(key.getBytes(), value.getBytes());
        client.put(entry, newVersion.getBytes());

        Entry entryGet = client.get(key.getBytes());
        assertArrayEquals(key.getBytes(), entryGet.getKey());
        assertArrayEquals(value.getBytes(), entryGet.getValue());
        assertArrayEquals(newVersion.getBytes(), entryGet.getEntryMetadata()
                .getVersion());

        client.delete(entryGet);
        Entry entryGetAfterDelete = client.get(key.getBytes());
        assertEquals(null, entryGetAfterDelete);
    }
}
