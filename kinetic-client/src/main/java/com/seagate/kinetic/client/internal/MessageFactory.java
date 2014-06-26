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

import kinetic.client.ClusterVersionFailureException;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticException;
import kinetic.client.VersionMismatchException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Algorithm;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;
import com.seagate.kinetic.proto.Kinetic.Message.KeyValue;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message.Range;

import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;
import com.seagate.kinetic.proto.Kinetic.Message.Synchronization;
import com.seagate.kinetic.proto.Kinetic.MessageOrBuilder;

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
        KineticMessage holder = new KineticMessage();

        // new message
        Message.Builder message = Message.newBuilder();

        // set proto message
        holder.setMessage(message);

        // set message type
        message.getCommandBuilder().getHeaderBuilder()
        .setMessageType(MessageType.PUT);

        // set KeyValue in body
        KeyValue.Builder kv = message.getCommandBuilder().getBodyBuilder()
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
                holder.setValue(entry.getValue());
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

        return holder;
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

        KineticMessage im = createKineticMessageWithBuilder();

        Message.Builder request = (Builder) im.getMessage();

        request.getCommandBuilder().getHeaderBuilder()
        .setMessageType(requestType);

        request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
        .setKey(ByteString.copyFrom(key));

        return im;
    }

    public static KineticMessage createGetKeyRangeMessage(byte[] startKey,
            boolean startKeyInclusive, byte[] endKey, boolean endKeyInclusive,
            int maxKeys, boolean reverse) throws KineticException {

        KineticMessage im = createKineticMessageWithBuilder();

        Message.Builder request = (Builder) im.getMessage();

        // set message type
        request.getCommandBuilder().getHeaderBuilder()
        .setMessageType(MessageType.GETKEYRANGE);

        Range.Builder op = request.getCommandBuilder().getBodyBuilder()
                .getRangeBuilder();

        op.setStartKey(ByteString.copyFrom(startKey));
        op.setEndKey(ByteString.copyFrom(endKey));
        op.setStartKeyInclusive(startKeyInclusive);
        op.setEndKeyInclusive(endKeyInclusive);
        op.setMaxReturned(maxKeys);
        op.setReverse(reverse);
        // request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder().get

        return im;
    }

    public static KineticMessage createGetMetadataMessage(byte[] key,
            MessageType requestType) throws KineticException {

        KineticMessage im = createGetRequestMessage(key, requestType);

        Message.Builder request = (Builder) im.getMessage();

        request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
        .setMetadataOnly(true);

        return im;
    }

    public static KineticMessage createDeleteRequestMessage(Entry entry)
            throws KineticException {

        KineticMessage im = createKineticMessageWithBuilder();

        Message.Builder request = (Builder) im.getMessage();

        // set message type
        request.getCommandBuilder().getHeaderBuilder()
        .setMessageType(MessageType.DELETE);

        // set key
        request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
        .setKey(ByteString.copyFrom(entry.getKey()));

        // set version
        byte[] version = entry.getEntryMetadata().getVersion();
        if (version != null && version.length > 0) {
            request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
            .setDbVersion(ByteString.copyFrom(version));
        }

        // set synchronization mode if not set
        if (request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
                .hasSynchronization() == false) {
            // logger.info("setting sync flag: " + synchronization.toString());
            request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
            .setSynchronization(synchronization);
        }

        // delete request
        return im;
    }

    public static KineticMessage createForceDeleteRequestMessage(byte[] key)
            throws KineticException {

        KineticMessage im = createKineticMessageWithBuilder();

        Message.Builder request = (Builder) im.getMessage();

        // set message typr
        request.getCommandBuilder().getHeaderBuilder()
        .setMessageType(MessageType.DELETE);

        // set key
        request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
        .setKey(ByteString.copyFrom(key));

        // set force bit
        request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
        .setForce(true);

        // set synchronization mode if not set
        if (request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
                .hasSynchronization() == false) {

            // logger.info("setting sync flag: " + synchronization.toString());
            request.getCommandBuilder().getBodyBuilder().getKeyValueBuilder()
            .setSynchronization(synchronization);
        }

        // delete request
        return im;
    }

    public static Entry responsetoEntry(KineticMessage holder) {

        Message response = (Message) holder.getMessage();

        if (response.getCommand().getStatus().getCode() == StatusCode.NOT_FOUND) {
            return null;
        }

        Entry entry = new Entry();

        KeyValue kv = response.getCommand().getBody().getKeyValue();

        // set key
        entry.setKey(kv.getKey().toByteArray());

        // set value
        if (holder.getValue() != null) {
            // entry.setValue(response.getValue().toByteArray());
            entry.setValue(holder.getValue());
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
            MessageOrBuilder response) {

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
        MessageType requestType = request.getMessage().getCommand().getHeader().getMessageType();
        //response message type
        MessageType responseType = reply.getMessage().getCommand().getHeader().getMessageType();
        
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
        if (!reply.getMessage().getCommand().hasStatus()) {
            throw new KineticException("No status was set");
        }

        //get status code
        StatusCode statusCode = reply.getMessage().getCommand().getStatus().getCode();
        
        //if success, all is fine.  simply return
        if (statusCode == StatusCode.SUCCESS) {
            return;
        }
        
        //set standard exception message 
        String errorMessage = "Kinetic Command Exception: "
                + statusCode
                + ": "
                + reply.getMessage().getCommand().getStatus()
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

        KineticMessage im = createKineticMessageWithBuilder();

        Message.Builder request = (Builder) im.getMessage();

        request.getCommandBuilder().getHeaderBuilder()
        .setMessageType(MessageType.NOOP);

        return im;
    }
    
    public static KineticMessage createFlushDataRequestMessage()
            throws KineticException {

        KineticMessage im = createKineticMessageWithBuilder();

        Message.Builder request = (Builder) im.getMessage();

        request.getCommandBuilder().getHeaderBuilder()
        .setMessageType(MessageType.FLUSHALLDATA);

        return im;
    }
    

    /**
     * create an internal message with empty builder message.
     *
     * @return an internal message with empty builder message
     */
    public static KineticMessage createKineticMessageWithBuilder() {

        // new instance of internal message
        KineticMessage im = new KineticMessage();

        // new builder message
        Message.Builder builder = Message.newBuilder();

        // set to im
        im.setMessage(builder);

        return im;
    }

}
