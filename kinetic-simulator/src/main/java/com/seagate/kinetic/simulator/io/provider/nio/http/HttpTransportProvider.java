/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.io.provider.nio.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.simulator.io.provider.spi.MessageService;
import com.seagate.kinetic.simulator.io.provider.spi.TransportProvider;

/**
 * Please note: This class is for evaluation only and in prototype state.
 *
 * @author chiaming
 */
public class HttpTransportProvider implements TransportProvider, Runnable {

    public final Logger logger = Logger.getLogger(HttpTransportProvider.class
            .getName());

    private int port = 8123;

    private ServerBootstrap bootstrap = null;

    private ChannelFuture channelFuture = null;

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;

    private HttpChannelInitializer msChannelInitializer = null;

    private MessageService lcservice = null;

    // I/O service thread (server socket)
    private Thread myThread = null;

    // my name
    private String myName = null;

    public HttpTransportProvider()
            throws InterruptedException {
        ;
    }

    public void doInit() throws InterruptedException {

        this.port = this.lcservice.getServiceConfiguration().getPort();

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        msChannelInitializer = new HttpChannelInitializer(
                this.lcservice);

        bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(msChannelInitializer);

        logger.info("KineticClient http service binding on port =" + port);

        channelFuture = bootstrap.bind(port).sync();

        this.myName = "ioMessageService-" + port;

        this.myThread = new Thread(this);
        this.myThread.setName(myName);
        this.myThread.start();
    }

    @Override
    public void run() {

        try {

            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void close() {
        logger.info("KineticClient http service closed, port =" + port);
    }

    @Override
    public void init(MessageService messageService) {

        this.lcservice = messageService;
    }

    @Override
    public void start() throws IOException {
        try {
            this.doInit();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void stop() {
        try {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            long awaitTimeout = this.lcservice.getServiceConfiguration()
                    .getThreadPoolAwaitTimeout();

            bossGroup.terminationFuture().await(awaitTimeout);
            workerGroup.terminationFuture().await(awaitTimeout);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        logger.info("KineticClient http service stopped, port =" + port);
    }

}
