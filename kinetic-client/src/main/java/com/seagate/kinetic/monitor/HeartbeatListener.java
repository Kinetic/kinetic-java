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
package com.seagate.kinetic.monitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    /**
     * Construct a heartbeat consumer with default address and port.
     *
     * @throws IOException
     */
    public HeartbeatListener() throws IOException {
        this.init();
    }

    public HeartbeatListener(String address, int port) throws IOException {

        this.mcastDestination = address;

        this.mcastPort = port;

        this.init();
    }

    private void init() throws IOException {

        // find network interface
        NetworkInterface ni = this.findNetworkInterface();

        // my multicast listening address
        mcastAddress = InetAddress.getByName(mcastDestination);

        // msocket
        mcastSocket = new MulticastSocket(mcastPort);

        // only set it if we are allowed to search
        if (ni != null) {
            mcastSocket.setNetworkInterface(ni);
        }

        // join the m group
        this.mcastSocket.joinGroup(mcastAddress);

        // listen in the background
        this.thread = new Thread(this);

        thread.start();
    }

    private NetworkInterface findNetworkInterface() {
        NetworkInterface ni = null;

        try {
            Enumeration<NetworkInterface> nis = NetworkInterface
                    .getNetworkInterfaces();

            while (nis.hasMoreElements()) {
                ni = nis.nextElement();
                if (ni.supportsMulticast() && ni.isUp()) {
                    logger.info("found interface that supports multicast: "
                            + ni.getDisplayName());
                    break;
                }
            }
        } catch (SocketException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        return ni;
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
