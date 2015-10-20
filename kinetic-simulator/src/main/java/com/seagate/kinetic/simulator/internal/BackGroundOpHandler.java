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
package com.seagate.kinetic.simulator.internal;

import java.util.logging.Logger;

import kinetic.client.KineticException;
import kinetic.simulator.SimulatorConfiguration;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.common.lib.MessageDigestUtil;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.Algorithm;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.simulator.persist.KVValue;
import com.seagate.kinetic.simulator.persist.Store;
import com.seagate.kinetic.simulator.persist.memory.KeyComparator;

/**
 * 
 * Back ground operation handler prototype implementation
 * <p>
 * The current implementation responds to the request with a SUCCESS status if it passed
 * minimal verifications.
 * 
 * @author chiaming
 *
 */
public abstract class BackGroundOpHandler {
    
    private final static Logger logger = Logger.getLogger(BackGroundOpHandler.class
            .getName());
    
    @SuppressWarnings({ "rawtypes" })
    public static void mediaScan(KineticMessage request,
            KineticMessage respond, SimulatorEngine engine)
            throws KVStoreException, KineticException {

        KeyComparator comparator = new KeyComparator();

        Command.Builder commandBuilder = (Command.Builder) respond.getCommand();

        // set reply type
        commandBuilder.getHeaderBuilder().setMessageType(
                MessageType.MEDIASCAN_RESPONSE);

        // set ack sequence
        commandBuilder.getHeaderBuilder().setAckSequence(
                request.getCommand().getHeader().getSequence());

        // max return key count
        int maxReturned = request.getCommand().getBody().getRange()
                .getMaxReturned();
        
        int MaxSupported = SimulatorConfiguration.getMaxSupportedKeyRangeSize();

        try {
            
            // if not set, set to default
            if (maxReturned <= 0) {
                maxReturned = MaxSupported;
            } else if (maxReturned > MaxSupported) {
                throw new InvalidRequestException(
                        "Exceed max returned key range., max allowed="
                                + SimulatorConfiguration
                                        .getMaxSupportedKeyRangeSize()
                                + ", request=" + maxReturned);
            }
            
            // check message type
            checkIsMessageValid (request);
            
            // check permission
            checkPermission (request, engine);  
            
            Store store = engine.getStore();

            /**
             *  XXX 09/09/2014 chiaming:
             *  framework to start background operation
             *  the job should be stoppable by a higher priority received
             *  before/after the long running bg ops.
             *  
             *  The following statements are for testing purpose only
             */
            
            // get start key
            ByteString startKey = request.getCommand().getBody().getRange()
                    .getStartKey();

            // get end key
            ByteString endKey = request.getCommand().getBody().getRange()
                    .getEndKey();

            byte[] endKeybytes = endKey.toByteArray();

            // if scan to the end of the map
            boolean toEndOfMap = false;
            if (endKey.isEmpty()) {
                toEndOfMap = true;
            }

            // finish scan flag
            boolean done = false;

            // kv entry
            KVValue kv = null;

            // check if start key inclusive
            if (request.getCommand().getBody().getRange()
                    .getStartKeyInclusive()) {
                // include start key
                kv = get(store, startKey);
                if (kv == null) {
                    kv = getNext(store, startKey);
                }

            } else {
                // get next key
                kv = getNext(store, startKey);
            }

            // scan the drive
            long index = 0;
            while (done == false) {

                if (kv != null) {
                    // get algo
                    Algorithm algo = kv.getAlgorithm();
                    // get tag
                    ByteString tag = kv.getTag();

                    // compare tag
                    logger.info((index++) + ": scan media for key: "
                            + kv.getKeyOf()
                            + ", algo: " + algo);

                    if ((tag != null)
                            && (tag.isEmpty() == false)
                            && MessageDigestUtil
                                    .isSupportedForKineticJava(algo)) {

                        ByteString ctag = MessageDigestUtil.calculateTag(algo,
                                kv.getData()
                                .toByteArray());

                        if (tag.equals(ctag) == false) {

                            logger.info("tag does not match for key: "
                                    + kv.getKeyOf() + ", algo: " + algo);

                            if (commandBuilder.getBodyBuilder()
                                    .getRangeBuilder().getKeysCount() < maxReturned) {
                                // add bad key
                                commandBuilder.getBodyBuilder()
                                        .getRangeBuilder()
                                        .addKeys(kv.getKeyOf());
                            } else {
                                // reached max returned keys
                                // set endkey in response
                                commandBuilder.getBodyBuilder()
                                        .getRangeBuilder()
                                        .setEndKey(kv.getKeyOf());

                                // finished scan
                                return;
                            }
                        } else {
                            logger.info("tag validated for key: "
                                    + kv.getKeyOf() + ", algo: " + algo);
                        }
                    }

                    // read next key
                    kv = getNext(store, kv.getKeyOf());

                    if (kv == null) {
                        // reached to end of map
                        done = true;
                    } else if (toEndOfMap == false) {

                        /**
                         * check if passed end key
                         */
                        if (comparator.compare(endKeybytes, kv.getKeyOf()
                                .toByteArray()) < 0) {
                            done = true;
                        }
                    }

                } else {
                    logger.info(index + ": scan media reached end of map");
                    done = true;
                }
            }

            // set endkey in response
            commandBuilder
                    .getBodyBuilder()
                    .getRangeBuilder()
                    .setEndKey(
                            request.getCommand().getBody().getRange()
                                    .getEndKey());

        } catch (KVSecurityException se) {
            commandBuilder.getStatusBuilder()
                    .setCode(StatusCode.NOT_AUTHORIZED);
            commandBuilder.getStatusBuilder().setStatusMessage(se.getMessage());
            logger.warning("unauthorized media scan opeartion request");
        } catch (InvalidRequestException ire) {
            commandBuilder.getStatusBuilder().setCode(
                    StatusCode.INVALID_REQUEST);
            commandBuilder.getStatusBuilder()
                    .setStatusMessage(ire.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    public static void mediaOptimize(KineticMessage request,
            KineticMessage respond, SimulatorEngine engine)
            throws KVStoreException, KineticException {

        Command.Builder commandBuilder = (Command.Builder) respond.getCommand();

        // set reply type
        commandBuilder.getHeaderBuilder().setMessageType(
                MessageType.MEDIAOPTIMIZE_RESPONSE);

        // set ack sequence
        commandBuilder.getHeaderBuilder().setAckSequence(
                request.getCommand().getHeader().getSequence());

        try {
            
            checkIsMessageValid (request);
            
            // check permission
            checkPermission (request, engine);  
            
            /**
             *  XXX 09/11/2014 chiaming:
             *  framework to start media optimize op
             *  the job should be stoppable by a higher priority received
             *  before/after the long running bg ops.
             *  
             *  The following statements are for testing purpose only
             */
            
            // get start key
            ByteString startKey = request.getCommand().getBody().getRange()
                    .getStartKey();

            // get end key
            ByteString endKey = request.getCommand().getBody().getRange()
                    .getEndKey();

            // ask store to do media compaction
            engine.getStore().compactRange(startKey, endKey);

            // set endkey in response
            commandBuilder
                    .getBodyBuilder()
                    .getRangeBuilder()
                    .setEndKey(
                            request.getCommand().getBody().getRange()
                                    .getEndKey());

        } catch (KVSecurityException se) {
            commandBuilder.getStatusBuilder()
                    .setCode(StatusCode.NOT_AUTHORIZED);
            commandBuilder.getStatusBuilder().setStatusMessage(se.getMessage());
            logger.warning("unauthorized media optimize opeartion request");
        } catch (InvalidRequestException ire) {
            commandBuilder.getStatusBuilder().setCode(
                    StatusCode.INVALID_REQUEST);
            commandBuilder.getStatusBuilder()
                    .setStatusMessage(ire.getMessage());
        }
    }
    
    private static void checkIsMessageValid (KineticMessage request) throws InvalidRequestException {
        
        MessageType mtype = request.getCommand().getHeader().getMessageType();
        
        switch (mtype) {
        case MEDIASCAN:
        case MEDIAOPTIMIZE:
            // XXX: more request message validation here
            return;
        default:
            throw new InvalidRequestException ("not a valid back ground op type: " + mtype.name());
        }
        
    }
    
    /**
     * check if user has range permission.
     * 
     * @param request
     * @param engine
     * @throws KVSecurityException
     */
    private static void checkPermission (KineticMessage request,
            SimulatorEngine engine) throws KVSecurityException {

        ByteString startKey = request.getCommand().getBody().getRange()
                .getStartKey();
        ByteString endKey = request.getCommand().getBody().getRange()
                .getEndKey();

        long user = request.getMessage().getHmacAuth().getIdentity();

        boolean hasPermission = Authorizer.hasRangePermission(
                engine.getAclMap(), user,
                Permission.RANGE, startKey, endKey);

        if (hasPermission == false) {
            throw new KVSecurityException(
                    "no permission for the requested range: "
                            + request.getCommand());
        } else {
            logger.info("range permission validate: " + request.getCommand());
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static KVValue get(Store store, ByteString key) {
        KVValue kv = null;

        try {
            kv = (KVValue) store.get(key);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        return kv;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static KVValue getNext(Store store, ByteString key) {
        KVValue kv = null;

        try {
            kv = (KVValue) store.getNext(key);
        } catch (KVStoreException e) {
            logger.info("no next key found for getNext");
        }

        return kv;
    }

}
