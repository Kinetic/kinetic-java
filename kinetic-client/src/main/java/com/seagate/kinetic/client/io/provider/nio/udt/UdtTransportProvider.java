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
package com.seagate.kinetic.client.io.provider.nio.udt;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.udt.UdtMessage;
import io.netty.channel.udt.nio.NioUdtProvider;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.client.io.provider.spi.ClientTransportProvider;
import com.seagate.kinetic.common.lib.KineticMessage;
//import com.seagate.kinetic.common.lib.NioThreadFactory;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;
import com.seagate.kinetic.proto.KineticIo.ExtendedMessage;

/**
 *
 * Please note: This class is for evaluation only and in prototype state.
 *
 * @author chiaming
 */
public class UdtTransportProvider implements ClientTransportProvider {

    public final Logger logger = Logger.getLogger(UdtTransportProvider.class
            .getName());

    private int port = 8123;

    private Bootstrap bootstrap = null;

    private EventLoopGroup workerGroup = null;

    private UdtClientChannelInitializer udtChannelInitializer = null;

    private ClientConfiguration config = null;

    private ClientMessageService mservice = null;

    private Channel channel = null;

    private String host = null;

    public UdtTransportProvider() {
        ;
    }

    private void initTransport() throws KineticException {

        this.port = this.config.getPort();

        this.host = this.config.getHost();

        try {

            workerGroup = UdtWorkerGroup.getWorkerGroup();

            udtChannelInitializer = new UdtClientChannelInitializer(
                    this.mservice);

            bootstrap = new Bootstrap();

            bootstrap.group(workerGroup)
            .channelFactory(NioUdtProvider.MESSAGE_CONNECTOR)
            .option(ChannelOption.SO_REUSEADDR, true)
            .handler(udtChannelInitializer);

            channel = bootstrap.connect(host, port).sync().channel();

        } catch (Exception e) {
            throw new KineticException(e);
        }

        logger.info("udt client connected to host:port =" + host
                + ":"
                + port);
    }


    @Override
    public void close() {

        logger.info("closing UDT message service., host=" + this.host
                + ", port=" + this.port);

        try {

            // close message handler
            this.mservice.close();

            // close channel
            if (this.channel != null) {
                this.channel.close();
            }

            UdtWorkerGroup.close();

        } catch (Exception e) {

            logger.log(Level.WARNING, e.getMessage(), e);
        }

        logger.info("KineticClient UDT client transport closed, url =" + host
                + ":" + port);
    }

    @Override
    public void init(ClientMessageService mservice)
            throws KineticException {

        this.config = mservice.getConfiguration();
        this.mservice = mservice;
        this.initTransport();
    }

    @Override
    public void write(KineticMessage km) throws IOException {

        // get interface message
        Message.Builder message = (Builder) km.getMessage();

        // create extended message
        ExtendedMessage.Builder extendedMessage = ExtendedMessage.newBuilder();

        // set interface message
        extendedMessage.setInterfaceMessage(message);

        // set optional value
        if (km.getValue() != null) {
            extendedMessage.setValue(ByteString.copyFrom(km.getValue()));
        }

        // get byte[] from extended message
        byte[] data = extendedMessage.build().toByteArray();

        final ByteBuf byteBuf = Unpooled.buffer(data.length);
        byteBuf.writeBytes(data);

        UdtMessage udt = new UdtMessage(byteBuf);

        logger.finest("writing udt message.size= " + byteBuf.readableBytes());

        this.channel.writeAndFlush(udt);
    }

}
