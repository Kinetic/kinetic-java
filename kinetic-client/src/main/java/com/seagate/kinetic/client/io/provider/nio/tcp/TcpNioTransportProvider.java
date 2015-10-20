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
package com.seagate.kinetic.client.io.provider.nio.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.client.io.provider.spi.ClientTransportProvider;
import com.seagate.kinetic.common.lib.KineticMessage;

/**
 * This class provides TCP nio transport support for the Kinetic client runtime.
 *
 * @auther James Hughes.
 * @author Chiaming Yang
 */
public class TcpNioTransportProvider implements ClientTransportProvider {

    // logger
    public final Logger logger = Logger.getLogger(TcpNioTransportProvider.class
            .getName());

    // default port
    private int port = 8123;

    private Bootstrap bootstrap = null;

    // private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;

    private NioChannelInitializer nioChannelInitializer = null;

    private ClientConfiguration config = null;

    private ClientMessageService mservice = null;

    private Channel channel = null;

    private String host = null;

    private static ShutdownHook shook = new ShutdownHook();

    static {
        Runtime.getRuntime().addShutdownHook(shook);
    }

    /**
     * Default constructor.
     */
    public TcpNioTransportProvider() {
        ;
    }

    private void initTransport() throws KineticException {

        this.port = this.config.getPort();
        this.host = this.config.getHost();

        try {

            workerGroup = NioWorkerGroup.getWorkerGroup();

            nioChannelInitializer = new NioChannelInitializer(this.mservice);

            bootstrap = new Bootstrap();

            bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                    .handler(nioChannelInitializer);

            if (config.getLocalAddress() == null) {
                channel = bootstrap.connect(host, port).sync().channel();
            } else {

                // remote address
                InetSocketAddress remote = new InetSocketAddress(host, port);

                // remote port
                InetSocketAddress local = new InetSocketAddress(
                        config.getLocalAddress(), config.getLocalPort());

                channel = bootstrap.connect(remote, local).sync().channel();

                logger.info("connected to remote with local address: "
                        + config.getLocalAddress() + ", local port="
                        + config.getLocalPort());
            }

        } catch (Exception e) {
            // release allocated resources
            this.close();
            throw new KineticException(e);
        }

        logger.info("TcpNio client transport provider connecting to host:port ="
                + host + ":" + port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {

        logger.info("closing tcp nio transport provider., host=" + this.host
                + ", port=" + this.port);

        try {

            // close message handler
            this.mservice.close();

            // close channel
            if (this.channel != null) {
                this.channel.close();
            }

            // release resources
            NioWorkerGroup.close();

        } catch (Exception e) {

            logger.log(Level.WARNING, e.getMessage(), e);
        }

        logger.info("Kinetic nio client transport provider closed, url ="
                + host + ":" + port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(ClientMessageService mservice) throws KineticException {

        this.mservice = mservice;

        this.config = mservice.getConfiguration();

        this.initTransport();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(KineticMessage message) throws IOException {
        this.channel.writeAndFlush(message);
    }

}
