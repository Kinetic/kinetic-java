/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.io.provider.nio.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;

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
public class HttpMessageServiceHandler extends
SimpleChannelInboundHandler<Object> {

    private static final Logger logger = Logger
            .getLogger(HttpMessageServiceHandler.class.getName());

    private ClientMessageService mservice = null;

    public HttpMessageServiceHandler(ClientMessageService mservice) {
        this.mservice = mservice;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        ExtendedMessage extendedMessage = null;

        int contentLength = 0;

        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("STATUS: " + response.getStatus());
                logger.finest("VERSION: " + response.getProtocolVersion());
            }

            String lenstr = response.headers().get(
                    HttpHeaders.Names.CONTENT_LENGTH);
            contentLength = Integer.parseInt(lenstr);

            if (!response.headers().isEmpty()) {
                for (String name : response.headers().names()) {
                    for (String value : response.headers().getAll(name)) {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("HEADER: " + name + " = " + value);
                        }
                    }
                }
            }
        }

        if (msg instanceof HttpContent) {

            HttpContent content = (HttpContent) msg;

            byte[] body = new byte[contentLength];
            content.content().getBytes(0, body);

            // created extended proto message
            extendedMessage = ExtendedMessage.newBuilder().mergeFrom(body)
                    .build();
            // new kinetic message
            KineticMessage km = new KineticMessage();

            // set interface message
            km.setMessage(extendedMessage.getInterfaceMessage());

            // set value
            if (extendedMessage.hasValue()) {
                km.setValue(extendedMessage.getValue().toByteArray());
            }

            this.mservice.routeMessage(km);
        }
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

        this.mservice.close();
    }

}
