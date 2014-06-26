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
package com.seagate.kinetic.client.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import kinetic.client.CallbackHandler;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;
import kinetic.client.advanced.PersistOption;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.internal.ClientProxy.KeyRange;
import com.seagate.kinetic.client.internal.ClientProxy.LCException;
import com.seagate.kinetic.client.lib.ClientLogger;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message.Synchronization;


/**
 *
 * Default KineticClient API implementation.
 * <p>
 *
 * @author James Hughes
 * @author Chiaming Yang
 */
public class DefaultKineticClient implements AdvancedKineticClient {

    // client logger
    private final static Logger LOG = ClientLogger.get();

    // client configuration
    private ClientConfiguration config = null;

    // client proxy -- perform ops for apps.
    private ClientProxy client = null;

    /**
     * Constructor to instantiate a new instance of kinetic client.
     *
     * @param config
     *            configuration for the new instance
     * @throws KineticException
     *             if any internal errors occur to instantiate a new instance.
     */
    public DefaultKineticClient(ClientConfiguration config)
            throws KineticException {
        this.config = config;
        // initialize the instance
        init();
    }

    /**
     * initialize the client instance.
     *
     * @throws KineticException
     *             if any internal errors occur to instantiate a new instance.
     */
    private void init() throws KineticException {

        // create client proxy to talk to the drive
        client = new ClientProxy(config);

        LOG.fine("kinetic client initialized, server=" + config.getHost()
                + ", port=" + config.getPort());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry put(Entry entry, byte[] newVersion) throws KineticException {
        return this.put(entry, newVersion, PersistOption.SYNC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry put(Entry entry, byte[] newVersion, PersistOption option)
            throws KineticException {

        // return entry
        Entry returnEntry = null;
        KineticMessage request = null;
        KineticMessage response = null;

        try {

            // construct put request message
            request = MessageFactory.createPutRequestMessage(
                    entry, newVersion);

            // proto builder
            Message.Builder message = (Builder) request.getMessage();

            // set persist option
            setPersistOption(message, option);

            // send request
            response = this.client.request(request);

            // check response
            //MessageFactory.checkPutReply(reply, MessageType.PUT_RESPONSE);

            // construct return instance
            returnEntry = new Entry(entry.getKey(), entry.getValue(),
                    entry.getEntryMetadata());

            // set db version in entry
            returnEntry.getEntryMetadata().setVersion(newVersion);

            LOG.fine("put versioned successfully.");

        } catch (KineticException lce) {
            throw lce;
        } catch (Exception e) {
            
            KineticException lce = new KineticException(e.getMessage(), e);
            lce.setRequestMessage(request);
            lce.setResponseMessage(response);
            
            throw lce;
        }

        return returnEntry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry get(byte[] key) throws KineticException {

        Entry entry = null;
        KineticMessage request = null;
        KineticMessage response = null;
        
        try {

            // create get request message
            request = MessageFactory.createGetRequestMessage(
                    key, MessageType.GET);

            // send request
            response = this.client.request(request);

            // check response
            //MessageFactory.checkGetReply(response, MessageType.GET_RESPONSE);

            // transform message to entry
            entry = MessageFactory.responsetoEntry(response);
        } catch (EntryNotFoundException enfe) {
            ;
        } catch (Exception e) {
            KineticException ke = new KineticException(e.getMessage(), e);
            ke.setRequestMessage(request);
            ke.setResponseMessage(response);
            throw ke;
        }

        return entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry getNext(byte[] key) throws KineticException {

        Entry entry = null;
        KineticMessage request = null;
        KineticMessage response = null;
        
        try {
            // create get request message
            request = MessageFactory.createGetRequestMessage(
                    key, MessageType.GETNEXT);

            // send request
            response = this.client.request(request);

            // check response
            //MessageFactory
            //.checkGetReply(response, MessageType.GETNEXT_RESPONSE);

            // transform message to entry
            entry = MessageFactory.responsetoEntry(response);
        } catch (EntryNotFoundException enfe) {
            ;
        } catch (Exception e) {
            KineticException lce = new KineticException(e.getMessage(), e);
            lce.setRequestMessage(request);
            lce.setResponseMessage(response);
            throw lce;
        }

        return entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry getPrevious(byte[] key) throws KineticException {

        Entry entry = null;
        KineticMessage request = null;
        KineticMessage response = null;

        try {

            // create get request message
            request = MessageFactory.createGetRequestMessage(
                    key, MessageType.GETPREVIOUS);

            // send request
            response = this.client.request(request);

            // check response
//            MessageFactory.checkGetReply(response,
//                    MessageType.GETPREVIOUS_RESPONSE);

            // transform message to entry
            entry = MessageFactory.responsetoEntry(response);
        } catch (EntryNotFoundException enfe) {
            ;
        } catch (Exception e) {
            KineticException lce = new KineticException(e.getMessage(), e);
            lce.setRequestMessage(request);
            lce.setResponseMessage(response);
            throw lce;
        }

        return entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<byte[]> getKeyRange(byte[] startKey, boolean startKeyInclusive,
            byte[] endKey, boolean endKeyInclusive, int maxReturned)
                    throws KineticException {

        // return response for the call
        List<byte[]> response = null;

        // default get key range
        boolean reverse = false;

        try {

            // get key range request parameter object
            KeyRange kr = client.new KeyRange(toByteString(startKey),
                    startKeyInclusive, toByteString(endKey), endKeyInclusive,
                    maxReturned, reverse);

            // send request to drive
            List<ByteString> bsList = this.client.getKeyRange(kr);

            // convert to return type
            response = toByteArrayList(bsList);
        } catch (KineticException ke) {
            throw ke;
        } catch (Exception e) {
            KineticException lce = new KineticException(e.getMessage(), e);
            throw lce;
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws KineticException {
        try {
            this.client.close();
        } catch (Exception e) {
            KineticException lce = new KineticException(e.getMessage(), e);
            throw lce;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Entry entry) throws KineticException {
        return this.delete(entry, PersistOption.SYNC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Entry entry, PersistOption option)
            throws KineticException {

        try {

            // create request message
            KineticMessage im = MessageFactory
                    .createDeleteRequestMessage(entry);

            // proto builder
            Message.Builder request = (Builder) im.getMessage();

            // set persist option
            setPersistOption(request, option);

            // do delete op
            return this.doDelete(im);

        } catch (KineticException lce) {
            throw lce;
        } catch (Exception e) {
            throw new KineticException(e);
        }
    }

    /**
     * perform delete operation.
     *
     * @param request
     * @return
     * @throws KineticException
     */
    private boolean doDelete(KineticMessage request) throws KineticException {
        
        //deleted flag
        boolean deleted = false;
        
        KineticMessage response = null;
        
        try {
            // response message
            response = this.client.request(request);

            //deleted = MessageFactory.checkDeleteReply(response);
            deleted = true;

            LOG.fine("delete entry successfully ...");
        } catch (EntryNotFoundException enfe) {
            //entry not found returns false for delete command
            deleted = false;
        } catch (KineticException ke) {
           //re-throw ke
           throw ke;
        } catch (Exception e) {
            KineticException lce = new KineticException(e.getMessage(), e);
            lce.setRequestMessage(request);
            lce.setResponseMessage(response);
            throw lce;
        }

        return deleted;
    }

    /**
     * Convert byte array to ByteString type.
     *
     * @param bytes
     *            byte array to be converted.
     *
     * @return converted ByteString.
     */
    public static ByteString toByteString(byte[] bytes) {
        return ByteString.copyFrom(bytes);
    }

    /**
     * Convert a list of ByteStrig type to a list of byte[] type.
     *
     * @param bsList
     *            a list of ByteStrig to be converted.
     * @return
     */
    public List<byte[]> toByteArrayList(List<ByteString> bsList) {

        List<byte[]> listOfByteArray = new ArrayList<byte[]>();

        for (ByteString bs : bsList) {
            listOfByteArray.add(bs.toByteArray());
        }

        return listOfByteArray;
    }

    /**
     * Send a request operation to the Kinetic server.
     * <p>
     *
     * @param request
     *            request message for a specific operation.
     *
     * @return respond message for the request operation.
     *
     * @throws LCException
     *             if any internal error occurred.
     */
    @Override
    public KineticMessage request(KineticMessage request)
            throws KineticException {
        KineticMessage respond = null;

        try {
            respond = this.client.request(request);
        } catch (KineticException ke) {
            throw ke;
        } catch (Exception e) {
            KineticException ke = new KineticException(e.getMessage(), e);
            ke.setRequestMessage(request);
            ke.setResponseMessage(respond);
            
            throw ke;
        }
        return respond;
    }

    /**
     * async request operation.
     *
     * @param <T>
     *
     * @param message
     *            request message.
     *
     * @param callback
     *            Kinetic client runtime calls the Callback.onMessage when
     *            response message is available.
     * @throws KineticException
     *             if any internal error occur.
     */
    @Override
    public <T> void requestAsync(KineticMessage message,
            CallbackHandler<T> callback) throws KineticException {

        try {
            this.client.requestAsync(message, callback);
        } catch (Exception e) {
            throw new KineticException(e.getMessage(), e);
        }
    }

    /**
     * Get an <code>Iterable</code> of <Entry> entry in the sequence based on
     * the specified key range.
     *
     * @param startKey
     *            the start key in the specified key range.
     * @param startKeyInclusive
     *            true if the start key is inclusive.
     * @param endKey
     *            the end key in the specified key range.
     * @param endKeyInclusive
     *            true if the start key is inclusive.
     *
     * @return an <code>Iterable</code> of <Entry> entry in the sequence based
     *         on the specified key range
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    public Iterable<Entry> getRange(byte[] startKey, boolean startKeyInclusive,
            byte[] endKey, boolean endKeyInclusive) throws KineticException {

        return new VersionedRange(this, startKey, startKeyInclusive, endKey,
                endKeyInclusive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAsync(Entry entry, byte[] newVersion,
            CallbackHandler<Entry> handler) throws KineticException {
        // default as sync
        this.putAsync(entry, newVersion, PersistOption.SYNC, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAsync(Entry entry, byte[] newVersion, PersistOption option,
            CallbackHandler<Entry> handler) throws KineticException {

        // construct put request message
        KineticMessage km = MessageFactory.createPutRequestMessage(entry,
                newVersion);
        // proto builder
        Message.Builder message = (Builder) km.getMessage();

        // set persist option
        setPersistOption(message, option);

        // send request to the drive
        this.client.requestAsync(km, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getAsync(byte[] key, CallbackHandler<Entry> handler)
            throws KineticException {

        // construct put request message
        KineticMessage message = MessageFactory.createGetRequestMessage(key,
                MessageType.GET);

        this.client.requestAsync(message, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAsync(Entry entry, CallbackHandler<Boolean> handler)
            throws KineticException {
        this.deleteAsync(entry, PersistOption.SYNC, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAsync(Entry entry, PersistOption option,
            CallbackHandler<Boolean> handler) throws KineticException {

        KineticMessage km = MessageFactory
                .createDeleteRequestMessage(entry);

        Message.Builder message = (Builder) km.getMessage();

        // set persist option
        setPersistOption(message, option);

        this.client.requestAsync(km, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getNextAsync(byte[] key, CallbackHandler<Entry> handler)
            throws KineticException {

        // construct put request message
        KineticMessage message = MessageFactory.createGetRequestMessage(key,
                MessageType.GETNEXT);

        this.client.requestAsync(message, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getPreviousAsync(byte[] key, CallbackHandler<Entry> handler)
            throws KineticException {

        // construct getPrevious request message
        KineticMessage message = MessageFactory.createGetRequestMessage(key,
                MessageType.GETPREVIOUS);

        this.client.requestAsync(message, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getKeyRangeAsync(byte[] startKey, boolean startKeyInclusive,
            byte[] endKey, boolean endKeyInclusive, int maxKeys,
            CallbackHandler<List<byte[]>> handler) throws KineticException {
        KineticMessage message = MessageFactory.createGetKeyRangeMessage(
                startKey, startKeyInclusive, endKey, endKeyInclusive, maxKeys,
                false);
        this.client.requestAsync(message, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntryMetadata getMetadata(byte[] key) throws KineticException {

        EntryMetadata metadata = null;
        KineticMessage request = null;
        KineticMessage response = null;
        
        try {

            // create get metadata request message
            request = MessageFactory.createGetMetadataMessage(
                    key, MessageType.GET);

            // send request
            response = this.client.request(request);

            // check response
            //MessageFactory.checkGetReply(response, MessageType.GET_RESPONSE);

            // transform message to metadata
            metadata = MessageFactory.responsetoEntryMetadata(response
                    .getMessage());
        } catch (EntryNotFoundException enfe) {
            ;
        } catch (Exception e) {
            KineticException ke = new KineticException(e.getMessage(), e);
            ke.setRequestMessage(request);
            ke.setResponseMessage(response);
            throw ke;
        }

        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getMetadataAsync(byte[] key,
            CallbackHandler<EntryMetadata> handler) throws KineticException {
        // construct get metadata request message
        KineticMessage message = MessageFactory.createGetMetadataMessage(key,
                MessageType.GET);

        this.client.requestAsync(message, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry putForced(Entry entry) throws KineticException {
        return this.putForced(entry, PersistOption.SYNC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry putForced(Entry entry, PersistOption option)
            throws KineticException {

        byte[] newVersion = null;
        
        KineticMessage request = null;
        KineticMessage response = null;

        if (entry.getEntryMetadata() != null) {
            newVersion = entry.getEntryMetadata().getVersion();
        }

        try {

            // construct put request message
            request = MessageFactory.createPutRequestMessage(
                    entry, newVersion);

            Message.Builder message = (Builder) request.getMessage();

            // set force bit
            message.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
            .setForce(true);

            // set persist option
            setPersistOption(message, option);

            // send request
            response = this.client.request(request);

            // check response
            //MessageFactory.checkPutReply(reply, MessageType.PUT_RESPONSE);

            LOG.fine("put versioned successfully.");

        } catch (KineticException lce) {
            throw lce;
        } catch (Exception e) {
            
            KineticException lce = new KineticException(e.getMessage(), e);
            lce.setRequestMessage(request);
            lce.setResponseMessage(response);
            
            throw lce;
        }

        return entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putForcedAsync(Entry entry, CallbackHandler<Entry> handler)
            throws KineticException {
        this.putForcedAsync(entry, PersistOption.SYNC, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putForcedAsync(Entry entry, PersistOption option,
            CallbackHandler<Entry> handler) throws KineticException {

        byte[] newVersion = null;

        if (entry.getEntryMetadata() != null) {
            newVersion = entry.getEntryMetadata().getVersion();
        }

        // construct put request message
        KineticMessage km = MessageFactory.createPutRequestMessage(entry,
                newVersion);

        Message.Builder message = (Builder) km.getMessage();

        // set force bit
        message.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
        .setForce(true);

        // set persist option
        setPersistOption(message, option);

        this.client.requestAsync(km, handler);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteForced(byte[] key) throws KineticException {
        return this.deleteForced(key, PersistOption.SYNC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteForced(byte[] key, PersistOption option)
            throws KineticException {

        // create force delete request message
        KineticMessage km = MessageFactory
                .createForceDeleteRequestMessage(key);

        Message.Builder request = (Builder) km.getMessage();

        // set persist option
        setPersistOption(request, option);

        // do delete
        return this.doDelete(km);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteForcedAsync(byte[] key, CallbackHandler<Boolean> handler)
            throws KineticException {
        this.deleteForcedAsync(key, PersistOption.SYNC, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteForcedAsync(byte[] key, PersistOption option,
            CallbackHandler<Boolean> handler) throws KineticException {

        // create force delete request message
        KineticMessage km = MessageFactory
                .createForceDeleteRequestMessage(key);

        Message.Builder request = (Builder) km.getMessage();

        // set persist option
        setPersistOption(request, option);

        // do async delete
        this.client.requestAsync(km, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long noop() throws KineticException {

        long time = System.currentTimeMillis();

        // create get request message
        KineticMessage request = MessageFactory.createNoOpRequestMessage();

        // send request
        this.client.request(request);

        // check response
        // MessageFactory.checkNoOpReply(response);

        // no op round trip time
        time = System.currentTimeMillis() - time;

        return time;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws KineticException {
            // create get request message
            KineticMessage request = MessageFactory.createFlushDataRequestMessage();

            // send request
            this.client.request(request);
    }

    /**
     * Set persist option flag to the protocol buffer message.
     *
     * @see PersistOption
     */
    private static void setPersistOption(Message.Builder message,
            PersistOption option) {

        if (option == null) {
            throw new NullPointerException("persist option cannot be null");
        }

        switch (option) {
        case SYNC:
            message.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
            .setSynchronization(Synchronization.WRITETHROUGH);
            break;
        case ASYNC:
            message.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
            .setSynchronization(Synchronization.WRITEBACK);
            break;
        case FLUSH:
            message.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
            .setSynchronization(Synchronization.FLUSH);
            break;
        default:
            message.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
            .setSynchronization(Synchronization.WRITETHROUGH);
            break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<byte[]> getKeyRangeReversed(byte[] startKey,
            boolean startKeyInclusive, byte[] endKey, boolean endKeyInclusive,
            int maxKeys) throws KineticException {

        // return type
        List<byte[]> response = null;

        // set reverse flag
        boolean reverse = true;

        try {

            // construct request parameter object
            KeyRange kr = client.new KeyRange(toByteString(startKey),
                    startKeyInclusive, toByteString(endKey), endKeyInclusive,
                    maxKeys, reverse);

            // send request
            List<ByteString> bsList = this.client.getKeyRange(kr);

            // convert to return type
            response = toByteArrayList(bsList);
        } catch (KineticException ke) {
            throw ke;
        } catch (Exception e) {
            KineticException lce = new KineticException(e.getMessage(), e);
            throw lce;
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getKeyRangeReversedAsync(byte[] startKey,
            boolean startKeyInclusive, byte[] endKey, boolean endKeyInclusive,
            int maxKeys, CallbackHandler<List<byte[]>> handler)
                    throws KineticException {

        KineticMessage message = MessageFactory.createGetKeyRangeMessage(
                startKey, startKeyInclusive, endKey, endKeyInclusive, maxKeys,
                true);

        this.client.requestAsync(message, handler);
    }

}
