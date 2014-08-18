/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.seagate.kinetic.simulator.protocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.KineticTestHelpers;
import com.seagate.kinetic.SimulatorOnly;

public class V2ProtocolTest extends IntegrationTestCase {

    private static byte[] key = KineticTestHelpers.toByteArray("key");
    private static byte[] value = KineticTestHelpers.toByteArray("value");
    private static Entry entry = new Entry(key, value);
    private static int port = Integer.parseInt(System.getProperty(
            "KINETIC_PORT", "8123"));
    private static int sslPort = Integer.parseInt(System.getProperty(
            "KINETIC_SSL_PORT", "8443"));

    private ClientConfiguration clientConfiguration;
    private KineticClient client;

    @Before
    public void init() {

        clientConfiguration = new ClientConfiguration();
        clientConfiguration.setPort(port);
    }

    @After
    public void close() {
        if (null != client) {
            try {
                client.close();
            } catch (KineticException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @SimulatorOnly
    public void v2TcpClient_V2NioSimulator_test() throws KineticException {
//        client = startV2TcpClient();
//
//        client.put(entry, null);
//
//        Entry entryGet = client.get(key);
//
//        if (null == entryGet) {
//            fail("v2TcpClient vs v2NioSimulator: get entry failed");
//        }
//
//        assertArrayEquals(value, entryGet.getValue());
//        assertTrue(client.delete(entry));
    }

    @Test
    @SimulatorOnly
    public void v2NioClient_V2NioSimulator_test() throws KineticException {
//        client = startV2NioClient();
//
//        client.put(entry, null);
//
//        Entry entryGet = client.get(key);
//
//        if (null == entryGet) {
//            fail("v2NioClient vs v2NioSimulator: get entry failed");
//        }
//
//        assertArrayEquals(value, entryGet.getValue());
//        assertTrue(client.delete(entry));
    }

    @Test
    @SimulatorOnly
    public void v2TcpClient_V2TcpSimulator_test() throws KineticException {
//        client = startV2TcpClient();
//
//        client.put(entry, null);
//
//        Entry entryGet = client.get(key);
//
//        if (null == entryGet) {
//            fail("v2TcpClient vs v2TcpSimulator: get entry failed");
//        }
//
//        assertArrayEquals(value, entryGet.getValue());
//        assertTrue(client.delete(entry));
    }

    @Test
    @SimulatorOnly
    public void v2NioClient_V2TcpSimulator_test() throws KineticException {
//        client = startV2NioClient();
//
//        client.put(entry, null);
//
//        Entry entryGet = client.get(key);
//
//        if (null == entryGet) {
//            fail("v2NioClient vs v2TcpSimulator: get entry failed");
//        }
//
//        assertArrayEquals(value, entryGet.getValue());
//        assertTrue(client.delete(entry));
    }

    @Test
    @SimulatorOnly
    public void v2SslClient_V2SslSimulator_test() throws KineticException {
//        client = startV2SslClient();
//
//        client.put(entry, null);
//
//        Entry entryGet = client.get(key);
//
//        if (null == entryGet) {
//            fail("v2SslClient vs v2SslSimulator: get entry failed");
//        }
//
//        assertArrayEquals(value, entryGet.getValue());
//        assertTrue(client.delete(entry));
    }

    private KineticClient startV2TcpClient() throws KineticException {
        // only support v2 protocol
        clientConfiguration.setUseNio(false);

        return KineticClientFactory.createInstance(clientConfiguration);
    }

    private KineticClient startV2NioClient() throws KineticException {
        // only support v2 protocol
        clientConfiguration.setUseNio(true);

        return KineticClientFactory.createInstance(clientConfiguration);
    }

    private KineticClient startV2SslClient() throws KineticException {
        // only support v2 protocol
        clientConfiguration.setUseSsl(true);
        clientConfiguration.setPort(sslPort);

        return KineticClientFactory.createInstance(clientConfiguration);
    }
}
