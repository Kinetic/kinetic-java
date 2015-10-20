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
package com.seagate.kinetic.monitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.NetUtil;

/**
 *
 * Kinetic applications may start an instance of this class and set a message
 * listener to it.
 * <p>
 * The {@link HeartbeatListener#onMessage(byte[])} will be invoked for each
 * message received by the subscriber.
 *
 * @author chiaming
 *
 */
public class HeartbeatListener implements Runnable {

    private final static Logger logger = Logger
            .getLogger(HeartbeatListener.class.getName());

    // multicast socket
    private MulticastSocket mcastSocket = null;

    private InetAddress mcastAddress = null;

    private String mcastDestination = "239.1.2.3";

    // destination port
    private int mcastPort = 8123;

    private boolean isClosed = false;

    private Thread thread = null;

    // net interface name
    private String interfaceName = null;

    /**
     * Construct a heartbeat consumer with default address and port.
     *
     * @throws IOException
     */
    public HeartbeatListener() throws IOException {
        this.init();
    }

    /**
     * Construct a heartbeat listener with the specified network interface name.
     * 
     * @param netInterfaceName
     * @throws IOException
     */
    public HeartbeatListener(String netInterfaceName) throws IOException {

        this.interfaceName = netInterfaceName;

        this.init();
    }

    public HeartbeatListener(String address, int port) throws IOException {

        this.mcastDestination = address;

        this.mcastPort = port;

        this.init();
    }

    private void init() throws IOException {

        // network interface
        NetworkInterface ni = null;

        if (this.interfaceName != null) {
            ni = NetworkInterface.getByName(interfaceName);
        } else {
            ni = NetUtil.findMulticastNetworkInterface();
        }

        // my multicast listening address
        mcastAddress = InetAddress.getByName(mcastDestination);

        // msocket
        mcastSocket = new MulticastSocket(mcastPort);

        // only set it if we are allowed to search
        if (ni != null) {
            logger.info("using network interface: " + ni.getDisplayName());
            mcastSocket.setNetworkInterface(ni);
        }

        // join the m group
        this.mcastSocket.joinGroup(mcastAddress);

        // listen in the background
        this.thread = new Thread(this);

        thread.start();
    }

    public void close() {
        this.isClosed = true;
        this.mcastSocket.close();
    }

    @Override
    public void run() {

        logger.info("Heart beat listener is ready on address: "
                + this.mcastDestination + ":" + this.mcastPort);

        while (isClosed == false) {

            try {
                byte[] data = new byte[64 * 1024];

                DatagramPacket packet = new DatagramPacket(data, data.length);

                this.mcastSocket.receive(packet);

                // deliver
                onMessage(packet.getData());

            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }

    }

    /**
     * Applications override this method to receive heart beat messages.
     *
     * @param data
     *            UTF8 encoded heart beat message.
     */
    public void onMessage(byte[] data) {

        try {
            logger.info("received heart beat: " + new String(data, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.warning(e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        @SuppressWarnings("unused")
        HeartbeatListener listener = new HeartbeatListener();
    }
}
