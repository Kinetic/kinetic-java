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
