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
package com.seagate.kinetic.client.io.provider.nio.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.client.io.provider.spi.ClientTransportProvider;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;
import com.seagate.kinetic.proto.KineticIo.ExtendedMessage;

/**
 *
 * Please note: This class is for evaluation only and in prototype state.
 *
 * @author chiaming
 */
public class HttpTransportProvider implements ClientTransportProvider {

    public final Logger logger = Logger.getLogger(HttpTransportProvider.class
            .getName());

    private int port = 8123;

    private Bootstrap bootstrap = null;

    private EventLoopGroup workerGroup = null;

    private HttpChannelInitializer nioChannelInitializer = null;

    private ClientConfiguration config = null;

    private ClientMessageService mservice = null;

    private Channel channel = null;

    private String host = null;

    public HttpTransportProvider() {
        ;
    }

    private void initTransport() throws KineticException {

        this.port = this.config.getPort();
        this.host = this.config.getHost();

        try {
            // bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();

            nioChannelInitializer = new HttpChannelInitializer(this.mservice);

            bootstrap = new Bootstrap();

            bootstrap.group(workerGroup).channel(NioSocketChannel.class)
            .handler(nioChannelInitializer);

            channel = bootstrap.connect(host, port).sync().channel();

        } catch (Exception e) {
            this.close();
            throw new KineticException(e);
        }

        logger.info("KineticClient http client connecting to host:port =" + host
                + ":"
                + port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {

        try {

            // close message handler
            this.mservice.close();

            // close channel
            if (this.channel != null) {
                this.channel.close();
            }

            workerGroup.shutdownGracefully();

            workerGroup.terminationFuture().await(
                    this.config.getThreadPoolAwaitTimeout());

        } catch (Exception e) {

            logger.log(Level.WARNING, e.getMessage(), e);
        }

        logger.info("KineticClient nio client transport closed, url =" + host
                + ":" + port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(ClientMessageService mservice)
            throws KineticException {

        this.config = mservice.getConfiguration();
        this.mservice = mservice;
        this.initTransport();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(KineticMessage km) throws IOException {

        // interface message
        Message.Builder message = (Builder) km.getMessage();

        // extended message
        ExtendedMessage.Builder extendedMessage = ExtendedMessage.newBuilder();

        // set interface message
        extendedMessage.setInterfaceMessage(message);

        // set optional value
        if (km.getValue() != null) {
            extendedMessage.setValue(ByteString.copyFrom(km.getValue()));
        }

        // get serialized bytes
        byte[] array = extendedMessage.build().toByteArray();

        // Prepare the HTTP request.
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST, "/kinetic",
                Unpooled.copiedBuffer(array));

        request.headers().set(HttpHeaders.Names.HOST, host);

        request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING,
                HttpHeaders.Values.BINARY);

        request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING,
                HttpHeaders.Values.GZIP);

        request.headers().set(CONNECTION, Values.KEEP_ALIVE);

        request.headers().set(CONTENT_TYPE, "application/octet-stream");

        request.headers().set(HttpHeaders.Names.CONTENT_ENCODING,
                HttpHeaders.Values.BINARY);

        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
                request.content().readableBytes());

        if (logger.isLoggable(Level.INFO)) {
            logger.info("writing http message, len="
                    + request.content().readableBytes());
        }

        try {
            this.channel.writeAndFlush(request);
        } finally {
            // request.release();
        }
    }

}
