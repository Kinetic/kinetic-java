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
package com.seagate.kinetic.simulator.io.provider.nio.http;

import static io.netty.handler.codec.http.HttpHeaders.getHost;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.KineticIo.ExtendedMessage;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

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

    private MessageService lcservice = null;

    public HttpMessageServiceHandler(MessageService lcservice2) {
        this.lcservice = lcservice2;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        int contentLength = 0;

        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;

            logger.finest("protocol version: " + request.getProtocolVersion());

            logger.finest("host: " + getHost(request, "unknown"));

            logger.finest("REQUEST_URI: " + request.getUri());

            List<Map.Entry<String, String>> headers = request.headers()
                    .entries();

            String lenstr = request.headers().get(
                    HttpHeaders.Names.CONTENT_LENGTH);
            contentLength = Integer.parseInt(lenstr);

            logger.finest("content length=" + contentLength);

            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> h : request.headers().entries()) {
                    String key = h.getKey();
                    String value = h.getValue();
                    logger.finest("HEADER: " + key + " = " + value);
                }
            }

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
                    request.getUri());
            Map<String, List<String>> params = queryStringDecoder.parameters();
            if (!params.isEmpty()) {
                for (Entry<String, List<String>> p : params.entrySet()) {
                    String key = p.getKey();
                    List<String> vals = p.getValue();
                    for (String val : vals) {
                        logger.finest("PARAM: " + key + " = " + val);
                    }
                }

            }

        }

        // create extended builder
        ExtendedMessage.Builder extendedMessage = ExtendedMessage.newBuilder();

        if (msg instanceof HttpContent) {

            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();
            if (content.isReadable()) {

                byte[] body = new byte[contentLength];
                content.getBytes(0, body);

                // read from serialized bytes
                extendedMessage.mergeFrom(body);
            }

            // build message
            ExtendedMessage extended = extendedMessage.build();

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("received request: " + extended);
            }

            // create kinetic message for processing
            KineticMessage km = new KineticMessage();

            // set interface message
            km.setMessage(extended.getInterfaceMessage());
            
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

            // set value
            if (extended.hasValue()) {
                km.setValue(extended.getValue().toByteArray());
            }

            // process request
            KineticMessage kmresp = this.lcservice.processRequest(km);

            // construct response message
            ExtendedMessage.Builder extendedResponse = ExtendedMessage
                    .newBuilder();

            // set interface message
            extendedResponse.setInterfaceMessage((Message.Builder) kmresp
                    .getMessage());

            // set value
            if (kmresp.getValue() != null) {
                extendedResponse.setValue(ByteString.copyFrom(kmresp
                        .getValue()));
            }

            // get serialized value
            ByteBuf data = Unpooled.copiedBuffer(extendedResponse.build()
                    .toByteArray());

            FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK, data);

            httpResponse.headers()
            .set(CONTENT_TYPE, "application/octet-stream");

            httpResponse.headers().set(HttpHeaders.Names.CONTENT_ENCODING,
                    HttpHeaders.Values.BINARY);

            httpResponse.headers().set(CONTENT_LENGTH,
                    httpResponse.content().readableBytes());

            httpResponse.headers().set(CONNECTION,
                    HttpHeaders.Values.KEEP_ALIVE);

            // send response message
            ctx.writeAndFlush(httpResponse);

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("wrote and flushed response: " + kmresp);
            }
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
    }

}
