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

import kinetic.client.ClusterVersionFailureException;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.EntryNotFoundException;
import kinetic.client.KineticException;
import kinetic.client.VersionMismatchException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.Algorithm;
import com.seagate.kinetic.proto.Kinetic.Command.KeyValue;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Range;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.proto.Kinetic.Command.Synchronization;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.AuthType;

/**
 * Kinetic Message factory for the Java API client runtime implementation.
 *
 * @author chiaming
 *
 */
public class MessageFactory {

    // persist synchronization mode
    private static Synchronization synchronization = Synchronization.WRITEBACK;

    // override system property
    static {
        boolean async = Boolean.getBoolean("kinetic.persist.async");
        if (async) {
            synchronization = Synchronization.WRITEBACK;
        }
    }

    public static KineticMessage createPutRequestMessage(Entry entry,
            byte[] newVersion) throws KineticException {

        // new message holder
        KineticMessage kineticMessage = new KineticMessage();

        // new message
        Message.Builder message = Message.newBuilder();
        message.setAuthType(AuthType.HMACAUTH);
        
        // create command builder
        Command.Builder commandBuilder = Command.newBuilder();
        
        // set proto message
        kineticMessage.setMessage(message);
        
        // set command
        kineticMessage.setCommand(commandBuilder);

        // set message type
        commandBuilder.getHeaderBuilder()
        .setMessageType(MessageType.PUT);

        // set KeyValue in body
        KeyValue.Builder kv = commandBuilder.getBodyBuilder()
                .getKeyValueBuilder();
        try {

            // set key
            kv.setKey(ByteString.copyFrom(entry.getKey()));

            // set db version
            byte[] dbversion = entry.getEntryMetadata().getVersion();
            if (dbversion != null && dbversion.length > 0) {
                kv.setDbVersion(ByteString.copyFrom(dbversion));
            }

            // set new version
            if (newVersion != null && newVersion.length > 0) {
                kv.setNewVersion(ByteString.copyFrom(newVersion));
            }

            // set value
            if (entry.getValue() != null) {
                // message.setValue(ByteString.copyFrom(entry.getValue()));
                kineticMessage.setValue(entry.getValue());
            }

            // set tag
            if (entry.getEntryMetadata().getTag() != null) {
                kv.setTag(ByteString
                        .copyFrom(entry.getEntryMetadata().getTag()));
            }

            if (entry.getEntryMetadata().getAlgorithm() != null) {
                kv.setAlgorithm(Algorithm.valueOf(entry.getEntryMetadata()
                        .getAlgorithm()));
            }

            // set synchronization mode if not set
            if (kv.hasSynchronization() == false) {
                // logger.info("setting sync flag: " +
                // synchronization.toString());
                kv.setSynchronization(synchronization);
            }

        } catch (Exception e) {
            KineticException lce = new KineticException(e.getMessage(), e);
            throw lce;
        }

        return kineticMessage;
    }

    public static boolean checkDeleteReply(KineticMessage request, KineticMessage reply)
            throws KineticException {
        
        try {
           checkReply (request, reply);  
        } catch (EntryNotFoundException nfe) {
            return false;
        } catch (KineticException ke) {
            throw ke;
        }
       
        return true;
    }

    public static KineticMessage createGetRequestMessage(byte[] key,
            MessageType requestType) throws KineticException {

        KineticMessage kineticMessage = createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) kineticMessage.getCommand();

        commandBuilder.getHeaderBuilder()
        .setMessageType(requestType);

        commandBuilder.getBodyBuilder().getKeyValueBuilder()
        .setKey(ByteString.copyFrom(key));

        return kineticMessage;
    }

    public static KineticMessage createGetKeyRangeMessage(byte[] startKey,
            boolean startKeyInclusive, byte[] endKey, boolean endKeyInclusive,
            int maxKeys, boolean reverse) throws KineticException {

        KineticMessage kineticMessage = createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) kineticMessage.getCommand();

        // set message type
        commandBuilder.getHeaderBuilder()
        .setMessageType(MessageType.GETKEYRANGE);

        Range.Builder op = commandBuilder.getBodyBuilder()
                .getRangeBuilder();

        op.setStartKey(ByteString.copyFrom(startKey));
        op.setEndKey(ByteString.copyFrom(endKey));
        op.setStartKeyInclusive(startKeyInclusive);
        op.setEndKeyInclusive(endKeyInclusive);
        op.setMaxReturned(maxKeys);
        op.setReverse(reverse);
        // request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder().get

        return kineticMessage;
    }

    public static KineticMessage createGetMetadataMessage(byte[] key,
            MessageType requestType) throws KineticException {

        KineticMessage kineticMessage = createGetRequestMessage(key, requestType);

        Command.Builder commandBuilder = (Command.Builder) kineticMessage.getCommand();

        commandBuilder.getBodyBuilder().getKeyValueBuilder()
        .setMetadataOnly(true);

        return kineticMessage;
    }

    public static KineticMessage createDeleteRequestMessage(Entry entry)
            throws KineticException {

        KineticMessage kineticMessage = createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) kineticMessage.getCommand();

        // set message type
        commandBuilder.getHeaderBuilder()
        .setMessageType(MessageType.DELETE);

        // set key
        commandBuilder.getBodyBuilder().getKeyValueBuilder()
        .setKey(ByteString.copyFrom(entry.getKey()));

        // set version
        byte[] version = entry.getEntryMetadata().getVersion();
        if (version != null && version.length > 0) {
            commandBuilder.getBodyBuilder().getKeyValueBuilder()
            .setDbVersion(ByteString.copyFrom(version));
        }

        // set synchronization mode if not set
        if (commandBuilder.getBodyBuilder().getKeyValueBuilder()
                .hasSynchronization() == false) {
            // logger.info("setting sync flag: " + synchronization.toString());
            commandBuilder.getBodyBuilder().getKeyValueBuilder()
            .setSynchronization(synchronization);
        }

        // delete request
        return kineticMessage;
    }

    public static KineticMessage createForceDeleteRequestMessage(byte[] key)
            throws KineticException {

        KineticMessage kineticMessage = createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) kineticMessage.getCommand();

        // set message type
        commandBuilder.getHeaderBuilder().setMessageType(MessageType.DELETE);

        // set key
        commandBuilder.getBodyBuilder().getKeyValueBuilder()
        .setKey(ByteString.copyFrom(key));

        // set force bit
        commandBuilder.getBodyBuilder().getKeyValueBuilder()
        .setForce(true);

        // set synchronization mode if not set
        if (commandBuilder.getBodyBuilder().getKeyValueBuilder()
                .hasSynchronization() == false) {
            // logger.info("setting sync flag: " + synchronization.toString());
            commandBuilder.getBodyBuilder().getKeyValueBuilder()
            .setSynchronization(synchronization);
        }

        // delete request
        return kineticMessage;
    }

    public static Entry responsetoEntry(KineticMessage kineticMessage) {

        Command response = (Command) kineticMessage.getCommand();

        if (response.getStatus().getCode() == StatusCode.NOT_FOUND) {
            return null;
        }

        Entry entry = new Entry();

        KeyValue kv = response.getBody().getKeyValue();

        // set key
        entry.setKey(kv.getKey().toByteArray());

        // set value
        if (kineticMessage.getValue() != null) {
            // entry.setValue(response.getValue().toByteArray());
            entry.setValue(kineticMessage.getValue());
        } else {
            entry.setValue(new byte[0]);
        }

        // set db version
        if (kv.getDbVersion() != null && kv.getDbVersion().size() > 0) {
            entry.getEntryMetadata()
            .setVersion(kv.getDbVersion().toByteArray());
        }

        if (kv.getTag() != null && kv.getTag().size() > 0) {
            entry.getEntryMetadata().setTag(kv.getTag().toByteArray());
        }

        if (kv.getAlgorithm() != null) {
            entry.getEntryMetadata().setAlgorithm(kv.getAlgorithm().toString());
        }

        return entry;
    }

    public static EntryMetadata responsetoEntryMetadata(
            KineticMessage response) {

        if (response.getCommand().getStatus().getCode() == StatusCode.NOT_FOUND) {
            return null;
        }

        EntryMetadata metadata = new EntryMetadata();

        KeyValue kv = response.getCommand().getBody().getKeyValue();

        // set db version
        if (kv.getDbVersion() != null && kv.getDbVersion().size() > 0) {
            metadata.setVersion(kv.getDbVersion().toByteArray());
        }

        if (kv.getTag() != null && kv.getTag().size() > 0) {
            metadata.setTag(kv.getTag().toByteArray());
        }

        if (kv.getAlgorithm() != null) {
            metadata.setAlgorithm(kv.getAlgorithm().toString());
        }

        return metadata;
    }
    
    /**
     * Check the response message status.
     * 
     * @param reply the response message from drive/simulator
     * 
     * @throws KineticException if status code is not equal to <code>StatusCode.SUCCESS</code>
     */
    public static void checkReply(KineticMessage request, KineticMessage reply)
            throws KineticException {
        
        //request message type
        MessageType requestType = request.getCommand().getHeader().getMessageType();
        //response message type
        MessageType responseType = reply.getCommand().getHeader().getMessageType();
        
        //check message type
        //see .proto for message type definition rules
        if (responseType.getNumber() != (requestType.getNumber()-1)) {
            
            String msg =
            "Received wrong message type., received="
                    + responseType
                    + ", expected=" + requestType;
            
            throw new KineticException (msg);
        }

        //check if contains status message
        if (!reply.getCommand().hasStatus()) {
            throw new KineticException("No status was set");
        }

        //get status code
        StatusCode statusCode = reply.getCommand().getStatus().getCode();
        
        //if success, all is fine.  simply return
        if (statusCode == StatusCode.SUCCESS) {
            return;
        }
        
        //set standard exception message 
        String errorMessage = "Kinetic Command Exception: "
                + statusCode
                + ": "
                + reply.getCommand().getStatus()
                        .getStatusMessage();
                        
        switch (statusCode) {
        
        case VERSION_MISMATCH:
            //throw version mismatch exception
            throw new VersionMismatchException(errorMessage);
        case VERSION_FAILURE:
            //throw cluster version exception
            throw new ClusterVersionFailureException (errorMessage);
        case NOT_FOUND:
            //entry not found
            throw new EntryNotFoundException (errorMessage);
        default:
            //throw normal kinetic exception
            throw new KineticException (errorMessage);
        }

    }

    public static KineticMessage createNoOpRequestMessage()
            throws KineticException {

        KineticMessage kineticMessage = createKineticMessageWithBuilder();

        Command.Builder request = (Command.Builder) kineticMessage.getCommand();

        request.getHeaderBuilder().setMessageType(MessageType.NOOP);

        return kineticMessage;
    }
    
    public static KineticMessage createGetVersionRequestMessage(byte[] key)
            throws KineticException {

        KineticMessage kineticMessage = createKineticMessageWithBuilder();

        Command.Builder request = (Command.Builder) kineticMessage.getCommand();

        request.getHeaderBuilder().setMessageType(MessageType.GETVERSION);

        request.getBodyBuilder().getKeyValueBuilder()
                .setKey(ByteString.copyFrom(key));

        return kineticMessage;
    }

    public static KineticMessage createFlushDataRequestMessage()
            throws KineticException {

        KineticMessage kineticMessage = createKineticMessageWithBuilder();

        Command.Builder request = (Command.Builder) kineticMessage.getCommand();

        request.getHeaderBuilder().setMessageType(MessageType.FLUSHALLDATA);

        return kineticMessage;
    }
    
    public static KineticMessage createStartBatchRequestMessage(int batchId)
            throws KineticException {

        KineticMessage kineticMessage = createKineticMessageWithBuilder();

        Command.Builder request = (Command.Builder) kineticMessage.getCommand();

        request.getHeaderBuilder().setMessageType(MessageType.START_BATCH);

        request.getHeaderBuilder().setBatchID(batchId);

        return kineticMessage;
    }

    public static KineticMessage createEndBatchRequestMessage(int batchId,
            int count)
            throws KineticException {

        KineticMessage kineticMessage = createKineticMessageWithBuilder();

        Command.Builder request = (Command.Builder) kineticMessage.getCommand();

        request.getHeaderBuilder().setMessageType(MessageType.END_BATCH);

        request.getHeaderBuilder().setBatchID(batchId);

        request.getBodyBuilder().getBatchBuilder().setCount(count);

        return kineticMessage;
    }

    public static KineticMessage createAbortBatchRequestMessage(int batchId)
            throws KineticException {

        KineticMessage kineticMessage = createKineticMessageWithBuilder();

        Command.Builder request = (Command.Builder) kineticMessage.getCommand();

        request.getHeaderBuilder().setMessageType(MessageType.ABORT_BATCH);

        request.getHeaderBuilder().setBatchID(batchId);

        return kineticMessage;
    }

    /**
     * create an internal message with empty builder message.
     *
     * @return an internal message with empty builder message
     */
    public static KineticMessage createKineticMessageWithBuilder() {

        // new instance of internal message
        KineticMessage kineticMessage = new KineticMessage();

        // new builder message
        Message.Builder message = Message.newBuilder();

        // set to im
        kineticMessage.setMessage(message);
        
        //set hmac auth type
        message.setAuthType(AuthType.HMACAUTH);
        
        // create command builder
        Command.Builder commandBuilder = Command.newBuilder();
        
        // set command
        kineticMessage.setCommand(commandBuilder);
        
        return kineticMessage;
    }

}
