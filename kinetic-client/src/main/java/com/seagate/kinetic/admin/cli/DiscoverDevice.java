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
package com.seagate.kinetic.admin.cli;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class DiscoverDevice {
    private Set<String> registerWwn = new HashSet<String>();

    public void registerWwn(String wwn) {
        registerWwn.add(wwn);
    }

    public boolean isRegisterWwn(String wwn) {
        return registerWwn.contains(wwn);
    }

    public class DeviceDiscoveryThread extends Thread {
        private MulticastSocket multicastSocket;

        public DeviceDiscoveryThread(MulticastSocket multicastSocket) {
            this.multicastSocket = multicastSocket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    registerNewKineticDevice();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public List<KineticDevice> registerNewKineticDevice()
                throws IOException {
            List<KineticDevice> newDiscoveriedKineticDevice = new ArrayList<KineticDevice>();
            byte[] b = new byte[64 * 1024];
            DatagramPacket p = new DatagramPacket(b, b.length);
            multicastSocket.receive(p);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readValue(p.getData(), JsonNode.class);
            String model = root.get("model").asText();
            String wwn = root.get("world_wide_name").asText();
            String serialNumber = root.get("serial_number").asText();
            int port = root.get("port").asInt();
            int tlsPort = root.get("tlsPort").asInt();

            if (!isRegisterWwn(wwn)) {
                registerWwn(wwn);
                JsonNode ifs = root.get("network_interfaces");
                List<String> inet4 = new ArrayList<String>();

                if (!ifs.isArray()) {
                    return newDiscoveriedKineticDevice;
                } else {
                    for (int i = 0; i < ifs.size(); i++) {
                        inet4.add(ifs.get(i).get("ipv4_addr").asText());
                    }
                }

                KineticDevice device = new KineticDevice(inet4, port, tlsPort,
                        wwn, model, serialNumber);
                System.out.println(device.toString());
                newDiscoveriedKineticDevice.add(device);
            }

            return newDiscoveriedKineticDevice;
        }
    }

    private void broadcastToDiscoverNodes() throws IOException {
        Enumeration<NetworkInterface> nets = NetworkInterface
                .getNetworkInterfaces();
        String mcastDestination = "239.1.2.3";
        int mcastPort = 8123;
        MulticastSocket multicastSocket;
        for (NetworkInterface netIf : Collections.list(nets)) {
            InetAddress iadd;
            iadd = InetAddress.getByName(mcastDestination);

            multicastSocket = new MulticastSocket(mcastPort);
            multicastSocket.setNetworkInterface(netIf);
            multicastSocket.joinGroup(iadd);
            new DeviceDiscoveryThread(multicastSocket).start();
        }
    }

    public static void main(String[] args) {
        long sleepTime = 60000;
        if (args.length > 1) {
            System.out.println("Parameter error!!!");
            System.out.println("Usage:");
            System.out.println("DeviceDiscovery [timeout(ms)]");
            System.out.println("Welcome to try again.");
            return;
        }
        if (args.length == 1) {
            sleepTime = Integer.parseInt(args[0]);
        }

        DiscoverDevice discoveryDevice = new DiscoverDevice();
        try {
            discoveryDevice.broadcastToDiscoverNodes();
            Thread.sleep(sleepTime);
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
