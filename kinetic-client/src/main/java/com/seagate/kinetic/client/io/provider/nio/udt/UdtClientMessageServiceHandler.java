/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.io.provider.nio.udt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.udt.UdtMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.KineticIo.ExtendedMessage;

/**
 *
 * Please note: This class is for evaluation only and in prototype state.
 *
 * @author chiaming
 *
 */
public class UdtClientMessageServiceHandler extends
SimpleChannelInboundHandler<UdtMessage> {

    private static final Logger logger = Logger
            .getLogger(UdtClientMessageServiceHandler.class.getName());

    private ClientMessageService mservice = null;

    public UdtClientMessageServiceHandler(ClientMessageService mservice) {
        this.mservice = mservice;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        ;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UdtMessage udtMessage)
            throws Exception {

        byte[] dst = new byte[udtMessage.content().readableBytes()];
        udtMessage.content().getBytes(0, dst);

        ExtendedMessage.Builder extendedBuilder = ExtendedMessage.newBuilder()
                .mergeFrom(dst);

        ExtendedMessage extendedMessage = extendedBuilder.build();

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("received request: " + extendedMessage);
        }

        // create kinetic message for processing
        KineticMessage km = new KineticMessage();
        km.setMessage(extendedMessage.getInterfaceMessage());
        if (extendedMessage.hasValue()) {
            km.setValue(extendedMessage.getValue().toByteArray());
        }

        this.mservice.routeMessage(km);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.log(Level.WARNING, "Unexpected exception from downstream.",
                cause);
        ctx.close();
    }

}
