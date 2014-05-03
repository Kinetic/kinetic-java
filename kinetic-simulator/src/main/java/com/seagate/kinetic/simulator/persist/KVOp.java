/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package com.seagate.kinetic.simulator.persist;

import java.util.Map;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;
import com.seagate.kinetic.proto.Kinetic.Message.KeyValue;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Message.Status;
import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;
import com.seagate.kinetic.proto.Kinetic.Message.Synchronization;
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
    
    //max key range size
    //private static final int MAX_KEY_RANGE_SIZE = 1024;
    
   //max version size
    //private static int MAX_VERSION_SIZE = 2048;
    
    static void oops(String s) throws KvException {
        oops(Status.StatusCode.INTERNAL_ERROR, s);
    }

    static void oops(Status.StatusCode status, String s) throws KvException {
        //LOG.warning("throwing exception., status code = " + status + ", msg = " +s);
        throw new KvException(status, s);
    }

    static void oops(Status.StatusCode status) throws KvException {
        throw new KvException(status, "");
    }

    public static void Op(Map<Long, ACL> aclmap,
            Store<ByteString, ByteString, KVValue> store, KineticMessage kmreq,
            KineticMessage kmresp) {

        Message request = (Message) kmreq.getMessage();

        Message.Builder respond = (Builder) kmresp.getMessage();

        try {

            // KV in;
            KeyValue requestKeyValue = request.getCommand().getBody()
                    .getKeyValue();

            // kv out
            KeyValue.Builder respondKeyValue = respond.getCommandBuilder()
                    .getBodyBuilder().getKeyValueBuilder();

            boolean metadataOnly = requestKeyValue.getMetadataOnly();

            // persist option
            PersistOption persistOption = getPersistOption(requestKeyValue);

            try {

                // set ack sequence
                respond.getCommandBuilder()
                .getHeaderBuilder()
                .setAckSequence(
                        request.getCommand().getHeader().getSequence());

                // key = in.getKey();
                ByteString key = requestKeyValue.getKey();

                KVValue storeEntry = null;

                // perform key value op
                switch (request.getCommand().getHeader().getMessageType()) {
                case GET:
                    // get entry from store
                    try {
                        
                        //check max key length
                        checkMaxKeyLenth (key.size());
                        
                        Authorizer.checkPermission(aclmap, request.getCommand()
                                .getHeader().getIdentity(), Permission.READ,
                                key);

                        storeEntry = store.get(key);

                        // respond metadata
                        respondKeyValue.setKey(storeEntry.getKeyOf());
                        respondKeyValue.setDbVersion(storeEntry.getVersion());
                        respondKeyValue.setTag(storeEntry.getTag());

                        respondKeyValue.setAlgorithm(storeEntry.getAlgorithm());

                        // respond value
                        if (!metadataOnly) {
                            // respond.setValue(storeEntry.getData());
                            //byte[] bytes = storeEntry.getData().toByteArray();
                            kmresp.setValue(storeEntry.getData().toByteArray());
                        }

                    } finally {
                        // respond message type
                        respond.getCommandBuilder().getHeaderBuilder()
                        .setMessageType(MessageType.GET_RESPONSE);
                    }
                    break;
                case PUT:

                    try {
                        
                      //check max key length
                       checkMaxKeyLenth (key.size());

                        if (isSupportedValueSize(kmreq) == false) {
                            throw new KvException(StatusCode.INVALID_REQUEST,
                                    "value size exceeded max supported size. Supported size: "
                                            + maxValueSize + ", received size="
                                            + kmreq.getValue().length
                                            + " (in bytes)");
                        }

                        Authorizer.checkPermission(aclmap, request.getCommand()
                                .getHeader().getIdentity(), Permission.WRITE,
                                key);

                        ByteString valueByteString = null;
                        if (kmreq.getValue() != null) {
                            valueByteString = ByteString.copyFrom(kmreq
                                    .getValue());
                        } else {
                            // set value to empty if null
                            valueByteString = ByteString.EMPTY;
                        }

                        KVValue data = new KVValue(requestKeyValue.getKey(),
                                requestKeyValue.getNewVersion(),
                                requestKeyValue.getTag(),
                                requestKeyValue.getAlgorithm(), valueByteString);

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
                        respond.getCommandBuilder().getHeaderBuilder()
                        .setMessageType(MessageType.PUT_RESPONSE);
                    }

                    break;
                case DELETE:

                    try {
                        
                        //check max key length
                        checkMaxKeyLenth (key.size());
                        
                        Authorizer.checkPermission(aclmap, request.getCommand()
                                .getHeader().getIdentity(), Permission.DELETE,
                                key);

                        if (requestKeyValue.getForce()) {
                            store.deleteForced(key, persistOption);
                        } else {
                            store.delete(requestKeyValue.getKey(),
                                    requestKeyValue.getDbVersion(), persistOption);
                        }

                    } finally {
                        // respond message type
                        respond.getCommandBuilder().getHeaderBuilder()
                        .setMessageType(MessageType.DELETE_RESPONSE);
                    }
                    break;
                case GETVERSION:
                    try {
                        
                        //check max key length
                        checkMaxKeyLenth (key.size());
                        
                        Authorizer.checkPermission(aclmap, request.getCommand()
                                .getHeader().getIdentity(), Permission.READ,
                                key);

                        storeEntry = store.get(key);
                        respondKeyValue.setDbVersion(storeEntry.getVersion());
                    } finally {
                        // respond message type
                        respond.getCommandBuilder()
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
                        Authorizer.checkPermission(aclmap, request.getCommand()
                                .getHeader().getIdentity(), Permission.READ,
                                nextKey);

                        respondKeyValue.setKey(nextKey);
                        respondKeyValue.setTag(storeEntry.getTag());
                        respondKeyValue.setDbVersion(storeEntry.getVersion());

                        respondKeyValue.setAlgorithm(storeEntry.getAlgorithm());

                        if (!metadataOnly) {
                            // respond.setValue(storeEntry.getData());
                            kmresp.setValue(storeEntry.getData().toByteArray());
                        }
                    } finally {
                        // respond message type
                        respond.getCommandBuilder().getHeaderBuilder()
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
                        Authorizer.checkPermission(aclmap, request.getCommand()
                                .getHeader().getIdentity(), Permission.READ,
                                previousKey);

                        respondKeyValue.setKey(previousKey);
                        respondKeyValue.setTag(storeEntry.getTag());
                        respondKeyValue.setDbVersion(storeEntry.getVersion());

                        respondKeyValue.setAlgorithm(storeEntry.getAlgorithm());

                        if (!metadataOnly) {
                            // respond.setValue(storeEntry.getData());
                            kmresp.setValue(storeEntry.getData().toByteArray());
                        }
                    } finally {
                        // respond message type
                        respond.getCommandBuilder()
                        .getHeaderBuilder()
                        .setMessageType(
                                MessageType.GETPREVIOUS_RESPONSE);
                    }

                    break;
                default:
                    oops("Unknown request");
                }

            } catch (KVStoreNotFound e) {
                oops(Status.StatusCode.NOT_FOUND);
            } catch (KVStoreVersionMismatch e) {
                oops(Status.StatusCode.VERSION_MISMATCH);
            } catch (KVStoreException e) {
                oops(Status.StatusCode.INTERNAL_ERROR,
                        "Opps1: " + e.getMessage());
            } catch (KVSecurityException e) {
                oops(StatusCode.NOT_AUTHORIZED, e.getMessage());
            } catch (InvalidRequestException e) {
                oops(StatusCode.INVALID_REQUEST, e.getMessage());
            } catch (Exception e) {
                oops(Status.StatusCode.INTERNAL_ERROR, e.getMessage());
            }

            // respond status
            respond.getCommandBuilder().getStatusBuilder()
            .setCode(Status.StatusCode.SUCCESS);

        } catch (KvException e) {

            LOG.fine("KV op Exception: " + e.status + ": " + e.getMessage());

            respond.getCommandBuilder().getStatusBuilder().setCode(e.status);
            respond.getCommandBuilder().getStatusBuilder()
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
    public static boolean isSupportedValueSize(KineticMessage km) {
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
     */
    public static PersistOption getPersistOption(KeyValue kv) {

        PersistOption option = PersistOption.SYNC;

        Synchronization sync = kv.getSynchronization();

        if (sync != null) {
            switch (sync) {
            case WRITETHROUGH:
            case FLUSH:
                option = PersistOption.SYNC;
                break;
            case WRITEBACK:
                option = PersistOption.ASYNC;
                break;
            default:
                option = PersistOption.SYNC;
            }

        }

        return option;
    }
    
    private static void checkMaxKeyLenth (int len) throws InvalidRequestException {
        if (len > maxKeySize) {
            throw new InvalidRequestException ("key length exceeds max size, size=" + maxKeySize);
        }
    }
    
//    private static void checkMaxKeyRange (int len) throws InvalidRequestException {
//        if (len > MAX_KEY_RANGE_SIZE) {
//            throw new InvalidRequestException ("max key range exceeds " + MAX_KEY_RANGE_SIZE);
//        }
//    }
//    
//    private static void checkMaxVersion (int len) throws InvalidRequestException {
//        if (len > MAX_VERSION_SIZE) {
//            throw new InvalidRequestException ("max version exceeds " + MAX_VERSION_SIZE);
//        }
//    }

}
