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

package com.seagate.kinetic.simulator.persist;

import java.util.Map;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.Algorithm;
import com.seagate.kinetic.proto.Kinetic.Command.KeyValue;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Status;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.proto.Kinetic.Command.Synchronization;
import com.seagate.kinetic.simulator.internal.Authorizer;
import com.seagate.kinetic.simulator.internal.InvalidRequestException;
import com.seagate.kinetic.simulator.internal.KVSecurityException;
import com.seagate.kinetic.simulator.internal.KVStoreException;
import com.seagate.kinetic.simulator.internal.KVStoreNotFound;
import com.seagate.kinetic.simulator.internal.KVStoreVersionMismatch;
import com.seagate.kinetic.simulator.lib.MyLogger;

class KvException extends Exception {
    private static final long serialVersionUID = -6541517825715118652L;
    Status.StatusCode status;

    KvException(Status.StatusCode status, String s) {
        super(s);
        this.status = status;
    }
}

public class KVOp {

    private final static Logger LOG = MyLogger.get();

    //max value size
    private static long maxValueSize = SimulatorConfiguration
            .getMaxSupportedValueSize();

    //max key size
    private static int maxKeySize = SimulatorConfiguration.getMaxSupportedKeySize();
    
   //max version size
    private static int maxVersionSize = SimulatorConfiguration.getMaxSupportedVersionSize();
    
    static void handleException(String s) throws KvException {
        handleException(Status.StatusCode.INTERNAL_ERROR, s);
    }

    static void handleException(Status.StatusCode status, String s) throws KvException {
        //LOG.warning("throwing exception., status code = " + status + ", msg = " +s);
        throw new KvException(status, s);
    }

    static void handleException(Status.StatusCode status) throws KvException {
        throw new KvException(status, "");
    }

    public static void processRequest(Map<Long, ACL> aclmap,
            Store<ByteString, ByteString, KVValue> store, KineticMessage kmreq,
            KineticMessage kmresp) {

        //Message request = (Message) kmreq.getMessage();

        Command.Builder commandBuilder = (Command.Builder) kmresp.getCommand();
        
        PersistOption persistOption = PersistOption.SYNC;

        try {

            // KV in;
            KeyValue requestKeyValue = kmreq.getCommand().getBody()
                    .getKeyValue();

            // kv out
            KeyValue.Builder respondKeyValue = commandBuilder
                    .getBodyBuilder().getKeyValueBuilder();

            boolean metadataOnly = requestKeyValue.getMetadataOnly();

            try {

                // set ack sequence
                commandBuilder
                .getHeaderBuilder()
                .setAckSequence(
                        kmreq.getCommand().getHeader().getSequence());

                // key = in.getKey();
                ByteString key = requestKeyValue.getKey();

                KVValue storeEntry = null;

                // perform key value op
                switch (kmreq.getCommand().getHeader().getMessageType()) {
                case GET:
                    // get entry from store
                    try {
                        
                        //check max key length
                        checkMaxKeyLenth (key.size());
                        
                        Authorizer.checkPermission(aclmap, kmreq.getMessage()
                                .getHmacAuth().getIdentity(),Permission.READ, key);

                        storeEntry = store.get(key);

                        // respond metadata
                        respondKeyValue.setKey(storeEntry.getKeyOf());
                        respondKeyValue.setDbVersion(storeEntry.getVersion());
                        respondKeyValue.setTag(storeEntry.getTag());

                        // set algorithm only if it was set by application
                        if (storeEntry.hasAlgorithm()) {
                            respondKeyValue.setAlgorithm(storeEntry.getAlgorithm());
                        }

                        // respond value
                        if (!metadataOnly) {
                            // respond.setValue(storeEntry.getData());
                            //byte[] bytes = storeEntry.getData().toByteArray();
                            kmresp.setValue(storeEntry.getData().toByteArray());
                        }

                    } finally {
                        // respond message type
                        commandBuilder.getHeaderBuilder()
                        .setMessageType(MessageType.GET_RESPONSE);
                    }
                    break;

                case PUT:

                    try {
                        
                        // persist option
                        persistOption = getPersistOption(requestKeyValue);

                        checkWrite(aclmap, kmreq, Permission.WRITE);

                        ByteString valueByteString = null;
                        if (kmreq.getValue() != null) {
                            valueByteString = ByteString.copyFrom(kmreq
                                    .getValue());
                        } else {
                            // set value to empty if null
                            valueByteString = ByteString.EMPTY;
                        }

                        Algorithm al = null;
                        if (requestKeyValue.hasAlgorithm()) {
                            al = requestKeyValue.getAlgorithm();
                        }
                        KVValue data = new KVValue(requestKeyValue.getKey(),
                                requestKeyValue.getNewVersion(),
                                requestKeyValue.getTag(),
                                al, valueByteString);

                        if (requestKeyValue.getForce()) {
                            store.putForced(key, data, persistOption);
                        } else {
                            // put to store
                            // data.setVersion(requestKeyValue.getNewVersion());
                            ByteString oldVersion = requestKeyValue
                                    .getDbVersion();
                            store.put(key, oldVersion, data, persistOption);
                        }
                    } finally {
                        // respond message type
                        commandBuilder.getHeaderBuilder()
                        .setMessageType(MessageType.PUT_RESPONSE);
                    }

                    break;
                case DELETE:

                    try {
                        
                        // persist option
                        persistOption = getPersistOption(requestKeyValue);

                        checkWrite(aclmap, kmreq, Permission.DELETE);

                        if (requestKeyValue.getForce()) {
                            store.deleteForced(key, persistOption);
                        } else {
                            store.delete(requestKeyValue.getKey(),
                                    requestKeyValue.getDbVersion(), persistOption);
                        }

                    } finally {
                        // respond message type
                        commandBuilder.getHeaderBuilder()
                        .setMessageType(MessageType.DELETE_RESPONSE);
                    }
                    break;
                case GETVERSION:
                    try {
                        
                        //check max key length
                        checkMaxKeyLenth (key.size());
                        
                        Authorizer.checkPermission(aclmap, kmreq.getMessage()
                                .getHmacAuth().getIdentity(), Permission.READ,
                                key);

                        storeEntry = store.get(key);
                        respondKeyValue.setDbVersion(storeEntry.getVersion());
                    } finally {
                        // respond message type
                        commandBuilder
                        .getHeaderBuilder()
                        .setMessageType(MessageType.GETVERSION_RESPONSE);
                    }
                    break;
                case GETNEXT:
                    try {
                        
                        //check max key length
                        checkMaxKeyLenth (key.size());
                        
                        storeEntry = store.getNext(key);
                        ByteString nextKey = storeEntry.getKeyOf();

                        // We must verify that the next key is readable, not the passed key
                        Authorizer.checkPermission(aclmap, kmreq.getMessage()
                                .getHmacAuth().getIdentity(), Permission.READ,
                                nextKey);

                        respondKeyValue.setKey(nextKey);
                        respondKeyValue.setTag(storeEntry.getTag());
                        respondKeyValue.setDbVersion(storeEntry.getVersion());
                        
                        if (storeEntry.hasAlgorithm()) {
                            respondKeyValue.setAlgorithm(storeEntry.getAlgorithm());
                        }

                        if (!metadataOnly) {
                            // respond.setValue(storeEntry.getData());
                            kmresp.setValue(storeEntry.getData().toByteArray());
                        }
                    } finally {
                        // respond message type
                        commandBuilder.getHeaderBuilder()
                        .setMessageType(MessageType.GETNEXT_RESPONSE);
                    }

                    break;
                case GETPREVIOUS:
                    try {
                        
                        //check max key length
                        checkMaxKeyLenth (key.size());
                        
                        storeEntry = store.getPrevious(key);
                        ByteString previousKey = storeEntry.getKeyOf();

                        // We must verify that the previous key is readable, not the passed key
                        Authorizer.checkPermission(aclmap, kmreq.getMessage()
                                .getHmacAuth().getIdentity(), Permission.READ,
                                previousKey);

                        respondKeyValue.setKey(previousKey);
                        respondKeyValue.setTag(storeEntry.getTag());
                        respondKeyValue.setDbVersion(storeEntry.getVersion());

                        if (storeEntry.hasAlgorithm()) {
                            respondKeyValue.setAlgorithm(storeEntry.getAlgorithm());
                        }

                        if (!metadataOnly) {
                            // respond.setValue(storeEntry.getData());
                            kmresp.setValue(storeEntry.getData().toByteArray());
                        }
                    } finally {
                        // respond message type
                        commandBuilder
                        .getHeaderBuilder()
                        .setMessageType(
                                MessageType.GETPREVIOUS_RESPONSE);
                    }

                    break;
                default:
                    handleException("Unknown request");
                }

            } catch (KVStoreNotFound e) {
                handleException(Status.StatusCode.NOT_FOUND);
            } catch (KVStoreVersionMismatch e) {
                handleException(Status.StatusCode.VERSION_MISMATCH);
            } catch (KVStoreException e) {
                handleException(Status.StatusCode.INTERNAL_ERROR,
                        "Opps1: " + e.getMessage());
            } catch (KVSecurityException e) {
                handleException(StatusCode.NOT_AUTHORIZED, e.getMessage());
            } catch (InvalidRequestException e) {
                handleException(StatusCode.INVALID_REQUEST, e.getMessage());
            } catch (Exception e) {
                handleException(Status.StatusCode.INTERNAL_ERROR, e.getMessage());
            }

            // respond status
            commandBuilder.getStatusBuilder()
            .setCode(Status.StatusCode.SUCCESS);

        } catch (KvException e) {

            LOG.warning ("KV op Exception: " + e.status + ": " + e.getMessage());

            commandBuilder.getStatusBuilder().setCode(e.status);
            commandBuilder.getStatusBuilder()
            .setStatusMessage(e.getMessage());
        }

    }

    /**
     * Check if request value size is within the max allowed size.
     *
     * @param request
     *            request message
     *
     * @return true if less than max allowed size. Otherwise, returned false.
     */
    private static boolean isSupportedValueSize(KineticMessage km) {
        boolean supported = false;

        byte[] value = km.getValue();

        if (value == null || value.length <= maxValueSize) {
            // value not set, this may happen if client library did not set
            // value as EMPTY for null value.
            supported = true;
        }

        return supported;
    }

    /**
     *
     * Get db persist option. Default is set to SYNC if not set.
     *
     * @param kv
     *            KeyValue element.
     *
     * @return persist option.
     * @throws KvException if invalid synchronization option is set in the kv parameter.
     */
    public static PersistOption getPersistOption(KeyValue kv)
            throws InvalidRequestException {
        
        /**
         * synchronization must be set to a valid value.
         */
        if (kv.hasSynchronization() == false) {
            throw new InvalidRequestException(
                    "Persistent synchronization option must be set to a valid value");
        }

        PersistOption option = PersistOption.SYNC;
        
        /**
         * if set, must be an valid option.
         */
        Synchronization sync = kv.getSynchronization();

        switch (sync) {
        case WRITETHROUGH:
        case FLUSH:
            option = PersistOption.SYNC;
            break;
        case WRITEBACK:
            option = PersistOption.ASYNC;
            break;
        default:
            throw new InvalidRequestException(
                    "Invalid persistent synchronization option: " + sync);
        }

        return option;
    }
    
    private static void checkMaxKeyLenth(int len)
            throws InvalidRequestException {
        if (len > maxKeySize) {
            throw new InvalidRequestException ("key length exceeds max size, size=" + maxKeySize + ", request size=" + len);
        }
    }
    
    private static void checkMaxVersionLength(ByteString bs)
            throws InvalidRequestException {
   
        int len = 0;
        if (bs != null) {
            len = bs.size();
        }
        
        if ( len > maxVersionSize) {
            throw new InvalidRequestException ("max version exceeds max allowed size " + maxVersionSize + ", request version size=" + len);
        }
    }

    /**
     * Check if request has write privilege, key length, version length.
     * 
     * @param aclmap
     *            acl map for this instance
     * @param kmreq
     *            request message
     * @param role
     *            role to access
     * @throws InvalidRequestException
     * @throws KVSecurityException
     */
    public static void checkWrite(Map<Long, ACL> aclmap, KineticMessage kmreq,
            Permission role)
            throws InvalidRequestException,
            KVSecurityException {

        KeyValue requestKeyValue = kmreq.getCommand().getBody().getKeyValue();

        ByteString key = requestKeyValue.getKey();

        // check max key length
        checkMaxKeyLenth(key.size());

        // check max version size
        ByteString bs = requestKeyValue.getNewVersion();

        checkMaxVersionLength(bs);

        if (isSupportedValueSize(kmreq) == false) {
            throw new InvalidRequestException(
                    "value size exceeded max supported size. Supported size: "
                            + maxValueSize + ", received size="
                            + kmreq.getValue().length + " (in bytes)");
        }

        // check permission
        Authorizer.checkPermission(aclmap, kmreq.getMessage().getHmacAuth()
                .getIdentity(), role, key);
    }

}
