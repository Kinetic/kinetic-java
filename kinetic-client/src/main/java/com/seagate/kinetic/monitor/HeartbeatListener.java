/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.monitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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

        // multicast group address
        mcastAddress = InetAddress.getByName(mcastDestination);

        mcastSocket = new MulticastSocket(mcastPort);

        this.mcastSocket.joinGroup(mcastAddress);

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
        HeartbeatListener listener = new HeartbeatListener();
    }
}
