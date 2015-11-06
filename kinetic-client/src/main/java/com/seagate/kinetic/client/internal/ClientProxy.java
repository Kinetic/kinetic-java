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
package com.seagate.kinetic.client.internal;

import java.io.IOException;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.spec.SecretKeySpec;

import kinetic.client.CallbackHandler;
import kinetic.client.ClientConfiguration;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.io.IoHandler;
import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.common.lib.Hmac.HmacException;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.Header;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Range;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.AuthType;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;


/**
 * Perform request response operation synchronously or asynchronously on behalf
 * of a Kinetic client.
 *
 * @see KineticClient
 * @see DefaultKineticClient
 *
 * @author James Hughes
 * @author Chiaming Yang
 */
public class ClientProxy {

    private final static Logger logger = Logger.getLogger(ClientProxy.class
            .getName());

    // client configuration
    private ClientConfiguration config = null;

    // client io handler
    private IoHandler iohandler = null;

    // user id
    private long user = 1;

    // connection id
    private long connectionID = 1234;

    // sequence
    private long sequence = 1;

    // cluster version
    private long clusterVersion = 43;

    // key associated with this client instance
    private Key myKey = null;

    // hmac key map
    private final Map<Long, Key> hmacKeyMap = new HashMap<Long, Key>();
    
    private volatile boolean isConnectionIdSetByServer = false;
    
    private CountDownLatch cidLatch = new CountDownLatch (1);

    private boolean isClosed = false;

    /**
     * Construct a new instance of client proxy
     *
     * @param config
     *            client configuration for the current instance
     * @throws KineticException
     *             if any internal error occurred.
     */
    public ClientProxy(ClientConfiguration config)
            throws KineticException {

        // client config
        this.config = config;

        // get user principal from client config
        user = config.getUserId();

        // build aclmap
        this.buildHmacKeyMap();

        // get cluster version from client config
        this.clusterVersion = config.getClusterVersion();

        // connection id
        this.connectionID = config.getConnectionId();

        // io handler
        this.iohandler = new IoHandler(this);
        
        if (this.iohandler.shouldWaitForStatusMessage()) {
            // wait for status message
            this.waitForStatusMessage();
        }
    }
    
    private void waitForStatusMessage() throws KineticException {
        
        try {
            this.cidLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        
        if (this.isConnectionIdSetByServer == false) {
            throw new KineticException("Hand shake failed with the service.");
        }
    }
    
    /**
     * Set client connection ID by server. his is set by  from the server. The 
     * client library uses this connectionID after this is set by the server.
     * 
     * @param cid the connection id to be set for this connection.
     */
    public void setConnectionId(KineticMessage kresponse) {

        if (this.isConnectionIdSetByServer) {
            /**
             * if already set by server, simply return.
             */
            return;
        }
        
        if (kresponse.getMessage().getAuthType() != AuthType.UNSOLICITEDSTATUS) {
            return;
        }

        /**
         * check if set connection ID is needed.
         */

        if (kresponse.getCommand().getHeader().hasConnectionID()) {

            this.connectionID = kresponse.getCommand().getHeader()
                    .getConnectionID();

            if (this.config.getExpectedWwn() != null) {

                String recvd = kresponse.getCommand().getBody().getGetLog()
                        .getConfiguration().getWorldWideName().toStringUtf8();

                if (config.getExpectedWwn().equals(recvd) == false) {

                    this.close();

                    logger.log(Level.SEVERE, "wwn does not match., expected="
                            + config.getExpectedWwn() + ", but received: "
                            + recvd);
                } else {
                    // set flag to true
                    this.isConnectionIdSetByServer = true;
                }
            } else {
                // set flag to true
                this.isConnectionIdSetByServer = true;
            }

            // count down
            this.cidLatch.countDown();
        }

    }

    /**
     * Get client configuration instance for this client instance.
     *
     * @return client configuration instance for this client instance.
     */
    public ClientConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * build client acl instance.
     *
     * @return acl instance.
     */
    private void buildHmacKeyMap() {

        Key key = new SecretKeySpec(ByteString.copyFromUtf8(config.getKey())
                .toByteArray(), "HmacSHA1");

        hmacKeyMap.put(config.getUserId(), key);

        this.myKey = key;
    }

    /*
     * KeyRange is a class the defines a range of keys.
     */
    class KeyRange {

        // start key and end key
        private ByteString startKey, endKey;

        // start key, end key, reverse boolean flags
        private boolean startKeyInclusive, endKeyInclusive, reverse;

        // max returned keys
        private int maxReturn;

        /**
         * Constructor for a new instance of key range.
         *
         * @param startKey
         *            the start key of key range
         * @param startKeyInclusive
         *            is start key inclusive
         * @param endKey
         *            end key of key range
         * @param endKeyInclusive
         *            is end key inclusive
         * @param maxReturned
         *            max allowed return keys.
         * @param reverse
         *            true if op is performed in reverse order
         */
        public KeyRange(ByteString startKey, boolean startKeyInclusive,
                ByteString endKey, boolean endKeyInclusive, int maxReturned,
                boolean reverse) {
            setStartKey(startKey);
            setStartKeyInclusive(startKeyInclusive);
            setEndKey(endKey);
            setEndKeyInclusive(endKeyInclusive);
            setMaxReturned(maxReturned);
            setReverse(reverse);
        }

        public ByteString getStartKey() {
            return startKey;
        }

        public void setStartKey(ByteString k1) {
            this.startKey = k1;
        }

        public ByteString getEndKey() {
            return endKey;
        }

        public void setEndKey(ByteString k2) {
            this.endKey = k2;
        }

        public boolean isStartKeyInclusive() {
            return startKeyInclusive;
        }

        public void setStartKeyInclusive(boolean i1) {
            this.startKeyInclusive = i1;
        }

        public boolean isEndKeyInclusive() {
            return endKeyInclusive;
        }

        public void setEndKeyInclusive(boolean i2) {
            this.endKeyInclusive = i2;
        }

        public int getMaxReturned() {
            return maxReturn;
        }

        public void setMaxReturned(int n) {
            this.maxReturn = n;
        }

        public boolean isReverse() {
            return reverse;
        }

        public void setReverse(boolean reverse) {
            this.reverse = reverse;
        }
    }

    /**
     * Returns a list of keys based on the key range specification.
     *
     * @param range
     *            specify the range of keys to be returned. This does not return
     *            the values.
     *
     * @return an array of keys in db that matched the specified range.
     *
     * @throws KineticException
     *             if any internal error occurred.
     *
     * @see KineticClient#getKeyRange(byte[], boolean, byte[], boolean, int)
     * @see KeyRange
     */
    List<ByteString> getKeyRange(KeyRange range) throws KineticException {
        
            // perform key range op
            KineticMessage resp = doRange(range);

            // return list of matched keys.
            return resp.getCommand().getBody().getRange()
                    .getKeysList();
    }

    /**
     * Perform range operation based on the specified key range specification.
     *
     * @param keyRange
     *            key range specification to be performed.
     * @return the response message from the range operation.
     *
     * @throws LCException
     *             if any internal error occurred.
     */
    KineticMessage doRange(KeyRange keyRange) throws KineticException {

        //request message
        KineticMessage request = null;
        // response message
        KineticMessage respond = null;
        
        try {
            // request message
            request = MessageFactory
                    .createKineticMessageWithBuilder();
            
            Command.Builder commandBuilder = (Command.Builder) request.getCommand();

            // set message type
            commandBuilder.getHeaderBuilder()
            .setMessageType(MessageType.GETKEYRANGE);

            // get range builder
            Range.Builder op = commandBuilder.getBodyBuilder()
                    .getRangeBuilder();

            // set parameters for the op
            op.setStartKey(keyRange.getStartKey());
            op.setEndKey(keyRange.getEndKey());
            op.setStartKeyInclusive(keyRange.isStartKeyInclusive());
            op.setEndKeyInclusive(keyRange.isEndKeyInclusive());
            op.setMaxReturned(keyRange.getMaxReturned());
            op.setReverse(keyRange.isReverse());

            // send request
            respond = request(request);
            
            MessageFactory.checkReply(request, respond);

            // return response
            return respond;
        } catch (KineticException ke) {
            //re-throw ke
            throw ke;
        } catch (Exception e) {
            //make a new kinetic exception
            KineticException ke = new KineticException (e);
            ke.setRequestMessage(request);
            ke.setResponseMessage(respond);
            //throw ke
            throw ke;
        }
    }

    public class LCException extends Exception {
        private static final long serialVersionUID = -6118533510243882800L;

        LCException(String s) {
            super(s);
        }
    }

    /**
     * Utility to throw internal LCException.
     *
     * @param exceptionMessage
     *            the message for the exception.
     *
     * @throws LCException
     *             the exception type to be thrown.
     */
    private void throwLcException(String exceptionMessage) throws LCException {
        throw new LCException(exceptionMessage);
    }
    
    /**
     * Send a kinetic request message to drive/simulator.
     * 
     * @param krequest the request message
     * @return response the response message 
     * @throws KineticException if the command operation failed.
     * 
     * @see kinetic.client.VersionMismatchException
     * @see kinetic.client.ClusterVersionFailureException
     */
    KineticMessage request(KineticMessage krequest) throws KineticException {
        
        KineticMessage kresponse = null;
        
        try {
            kresponse = this.doRequest(krequest);
            
            //check status code
            MessageFactory.checkReply(krequest, kresponse);
        } catch (KineticException ke) {
            ke.setRequestMessage(krequest);
            ke.setResponseMessage(kresponse);
            throw ke;
        } catch (Exception e) {
            throwKineticException (e, krequest, kresponse);
        }
        
        return kresponse;
    }
    
    private void throwKineticException(Exception e, KineticMessage request,
            KineticMessage response) throws KineticException {

        //new instance
        KineticException ke = new KineticException (e);
        
        //set request message
        ke.setRequestMessage(request);
        //set response message
        ke.setResponseMessage(response);
        
        throw ke;
    }

    /**
     * Send the specified request message synchronously to the Kinetic service.
     *
     * @param message
     *            the request message from the client.
     *
     * @return the response message from the service.
     *
     * @throws LCException
     *             if any errors occur.
     *
     * @see #requestAsync(com.seagate.kinetic.proto.Kinetic.Message.Builder,
     *      CallbackHandler)
     */
    KineticMessage doRequest(KineticMessage kmreq) throws LCException {
        
        // response kinetic message
        KineticMessage kmresp = null;
        
        try {

            // require to obtain lock to prevent possible dead-lock
        	// such as if connection close is triggered from remote.
        	synchronized (this) {
        		kmresp = this.iohandler.getMessageHandler().write(kmreq);
        	}

            // check if we do received a response
            if (kmresp == null) {
                throwLcException("Timeout - unable to receive response message within " + config.getRequestTimeoutMillis() + " ms");
            }

            // check hmac if this is a hmac auth type
            if (kmreq.getMessage().getAuthType() == AuthType.HMACAUTH) {
                if (!Hmac.check(kmresp, myKey)) {
                    throwLcException("Hmac failed compare");
                }
            }

        } catch (LCException lce) {
            // re-throw
            throw lce;
        } catch (HmacException e) {
            throwLcException("Hmac failed compute");
        } catch (java.net.SocketTimeoutException e) {
            throwLcException("Socket Timeout");
        } catch (IOException e1) {
            throwLcException("IO error");
        } catch (InterruptedException ite) {
            throwLcException(ite.getMessage());
        }

        return kmresp;
    }

    /**
     *
     * Send the specified request message asynchronously to the Kinetic service.
     *
     * @param message
     *            the request message to be sent asynchronously to the Kinetic
     *            service.
     *
     * @param handler
     *            the callback handler for the asynchronous request.
     *
     * @throws KineticException
     *             if any internal error occur.
     *
     * @see CallbackHandler
     * @see #request(com.seagate.kinetic.proto.Kinetic.Message.Builder)
     */
    <T> void requestAsync(KineticMessage kineticMessage, CallbackHandler<T> handler)
            throws KineticException {

        try {

            // create context message for the async operation
            CallbackContext<T> context = new CallbackContext<T>(handler);

            // set request message to the context so we can get it when response
            // is received
            context.setRequestMessage(kineticMessage);

            // send the async request message
            this.iohandler.getMessageHandler().writeAsync(kineticMessage, context);

        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw new KineticException(e.getMessage());
        }
    }

    void requestNoAck(KineticMessage kmreq) throws KineticException {

        try {
            // finalizeHeader(kmreq);
            this.iohandler.getMessageHandler().writeNoAck(kmreq);
        } catch (Exception e) {

            KineticException ke = new KineticException(e.getMessage());
            ke.setRequestMessage(kmreq);

            throw ke;
        }
    }

    /**
     * Check hmac based on the specified message.
     *
     * @param message
     *            the protocol buffer message from which hmac value is
     *            validated.
     *
     * @return true if hmac is validate. Otherwise, return false.
     */
    public boolean checkHmac(KineticMessage message) {
        boolean flag = false;

        try {
            //Hmac.check(message, this.myKey);
            byte[] bytes = message.getMessage().getCommandBytes().toByteArray();
            Hmac.check(bytes, this.myKey, message.getMessage().getHmacAuth().getHmac());
            
            flag = true;
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        return flag;
    }

    /**
     * Filled in required header fields for the request message.
     *
     * @param message
     *            the request protocol buffer message.
     */
    public void finalizeHeader(KineticMessage kineticMessage) {

        Message.Builder messageBuilder = (Builder) kineticMessage.getMessage();
        
        Command.Builder commandBuilder = (Command.Builder) kineticMessage.getCommand();

        // get header builder
        Header.Builder header = commandBuilder.getHeaderBuilder();

        // set cluster version
        header.setClusterVersion(clusterVersion);

        // set connection id.
        header.setConnectionID(connectionID);

        // set sequence number.
        header.setSequence(getNextSequence());

        /**
         * calculate and set tag value for the message
         */
        if (header.getMessageType() == MessageType.PUT) {
            if (commandBuilder.getBodyBuilder().getKeyValueBuilder().hasTag() == false) {
                // set tag to empty for backward compatibility with drive.
                // this can be removed when drive does not require the tag
                // to be set.
                commandBuilder.getBodyBuilder().getKeyValueBuilder()
                        .setTag(ByteString.EMPTY);

                // commandBuilder.getBodyBuilder().getKeyValueBuilder()
                // .setAlgorithm(Algorithm.INVALID_ALGORITHM);
            }

        }

        /**
         * calculate and set hmac value for this message
         */
        
        // get command byte string
        ByteString commandByteString = commandBuilder.build().toByteString();
        
        // get command bytes for hmac calculation
        byte[] commandBytes = commandByteString.toByteArray();
        
        // calculate HMAC
        try {

            if (messageBuilder.getAuthType() == AuthType.HMACAUTH) {
                // calculate hmac
                ByteString hmac = Hmac.calc(commandBytes, myKey);
                // set identity
                messageBuilder.getHmacAuthBuilder().setIdentity(user);
                // set hmac
                messageBuilder.getHmacAuthBuilder().setHmac(hmac);
            }

            // set command bytes to message
            messageBuilder.setCommandBytes(ByteString.copyFrom(commandBytes));

        } catch (HmacException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Get next sequence number for this connection (client instance).
     *
     * @return next unique number for this client instance.
     */
    private synchronized long getNextSequence() {
        return sequence++;
    }

    /**
     * close io handler and release associated resources.
     */
    public synchronized void close() {

        if (this.isClosed) {
            return;
        }

        try {
            if (this.iohandler != null) {
                iohandler.close();
            }
        } finally {
            this.isClosed = true;
        }

    }

}
