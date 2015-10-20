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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.udt.UdtMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
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
        
     // get command bytes
        ByteString commandBytes = extendedMessage.getInterfaceMessage().getCommandBytes();
        
        // build command
        Command.Builder commandBuilder = Command.newBuilder();
        
        try {
            commandBuilder.mergeFrom(commandBytes);
            km.setCommand(commandBuilder.build());
        } catch (InvalidProtocolBufferException e) {
           logger.log(Level.WARNING, e.getMessage(), e);
        }
        
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
