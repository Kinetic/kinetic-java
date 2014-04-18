package com.seagate.kinetic.simulator.console.one;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.SimulatorOnly;

public class MultiClientConnectToOneSimulatorTest extends IntegrationTestCase {
    // This number should be changed when real test, now the maximum number is
    // 10,000 if simulator and client run in two jvm.
    // the maximum number is 5,000 if simulator and client run in the same jvm.
    private static int CLIENT_COUNT = 100;
    private String key = "hello";
    private String value = "world";
    private String newVersion = "0";

    private List<KineticClient> clients;

    @Before
    public void setUp() throws Exception {
        clients = new ArrayList<KineticClient>();
        for (int i = 0; i < CLIENT_COUNT; i++) {
            KineticClient client = KineticClientFactory
                    .createInstance(getClientConfig());
            clients.add(client);
        }
    }

    @After
    public void tearDown() throws Exception {
        for (KineticClient client : clients) {
            client.close();
        }
    }

    @Test
    @SimulatorOnly
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
