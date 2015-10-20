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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Range;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Status;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;
import com.seagate.kinetic.simulator.internal.Authorizer;
import com.seagate.kinetic.simulator.internal.InvalidRequestException;
import com.seagate.kinetic.simulator.internal.KVSecurityException;
import com.seagate.kinetic.simulator.lib.MyLogger;

class RangeException extends Exception {
    private static final long serialVersionUID = -6541517825715118652L;

    Status.StatusCode status;

    RangeException(Status.StatusCode status, String s) {
        super(s);
        this.status = status;
    }
}

public class RangeOp {

    private final static Logger LOG = MyLogger.get();
    
    //max key range size.  
    private static int maxRangeSize = SimulatorConfiguration.getMaxSupportedKeyRangeSize();

    static void oops(String s) throws RangeException {
        oops(Status.StatusCode.INTERNAL_ERROR, s);
    }

    static void oops(Status.StatusCode status, String s) throws RangeException {
        throw new RangeException(status, s);
    }

    static void oops(Status.StatusCode status) throws RangeException {
        throw new RangeException(status, "");
    }

    @SuppressWarnings("unchecked")
    public static void operation(Store<ByteString, ByteString, KVValue> store,
            KineticMessage request, KineticMessage respond, Map<Long, Command.Security.ACL> aclMap) {

        ByteString k1 = null, k2 = null;
        boolean i1, i2, reverse;
        int n;

        Command.Builder commandBuilder = (Command.Builder) respond.getCommand();
        
        try {
             
            try {

                Range r = request.getCommand().getBody().getRange();

                k1 = r.getStartKey();

                LOG.fine("k1: " + Hmac.toString(k1));

                k2 = r.getEndKey();

                LOG.fine("k2: " + Hmac.toString(k2));

                i1 = r.getStartKeyInclusive();

                i2 = r.getEndKeyInclusive();
                n = r.getMaxReturned();
                
                //check max key range size
                checkMaxKeyRange (n);

                reverse = r.getReverse();


                if (n < 1) {
                    oops("the number of entries is <= 0");
                }

                List<KVKey> kvKeys = null;

                switch (request.getCommand().getHeader().getMessageType()) {
                case GETKEYRANGE:

                    // check permission
                    Authorizer.checkPermission(aclMap, request.getMessage()
                            .getHmacAuth().getIdentity(), Permission.RANGE, k1);

                    Authorizer.checkPermission(aclMap, request.getMessage()
                            .getHmacAuth().getIdentity(), Permission.RANGE, k2);

                    if (reverse) {
                        List<KVKey> l = (ArrayList<KVKey>) store.getRangeReversed(
                                k1, i1, k2, i2, n);
                        LOG.fine("getKeyRangeReversed returned " + l.size() + " entries");

                        kvKeys = filterRawKeysToAuthorizedKeys(l, request.getMessage().getHmacAuth().getIdentity(), aclMap);
                    } else {
                        SortedMap<KVKey, KVValue> m = (SortedMap<KVKey, KVValue>) store
                                .getRange(k1, i1, k2, i2, n);
                        LOG.fine("getKeyRange returned " + m.size() + " entries");

                        kvKeys = filterRawKeysToAuthorizedKeys(m.keySet(),
                                request.getMessage().getHmacAuth().getIdentity(),
                                aclMap);
                    }
                    break;
                default:
                    oops("Unknown request");
                }

                for (KVKey kvKey : kvKeys) {
                    LOG.fine("key="
                            + Hmac.toString(kvKey.toByteString()));

                    commandBuilder.getBodyBuilder()
                    .getRangeBuilder()
                    .addKeys(kvKey.toByteString());

                }

                // set ack sequence
                commandBuilder
                .getHeaderBuilder()
                .setAckSequence(
                        request.getCommand().getHeader().getSequence());

                // set status
                commandBuilder.getStatusBuilder()
                .setCode(Status.StatusCode.SUCCESS);

                // TODO check multi-tenant key prefix
            } catch (InvalidRequestException  ire) {
                oops(Status.StatusCode.INVALID_REQUEST,
                        ire.getMessage());
            } catch (KVSecurityException se) {
                oops(StatusCode.NOT_AUTHORIZED, se.getMessage());
            } catch (Exception e) {
                LOG.fine(e.toString());
                Writer writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                LOG.fine(writer.toString());

                oops(Status.StatusCode.INTERNAL_ERROR,
                        "Opps2: " + e.getMessage() + "--" + e.toString());
            }
       
            
        } catch (RangeException e) {
            commandBuilder.getStatusBuilder().setCode(e.status);
            commandBuilder.getStatusBuilder()
            .setStatusMessage(e.getMessage());
        } finally {

            switch (request.getCommand().getHeader().getMessageType()) {
            case GETKEYRANGE:
                commandBuilder.getHeaderBuilder()
                .setMessageType(MessageType.GETKEYRANGE_RESPONSE);
                break;
            default:
                break;
            }
        }
    }

    /**
     * Iterates over given keys and builds a list of the first contiguous block of keys for which the given use
     * has the RANGE role. Upon finding the first key in the given range that is not allowed, short circuits and
     * returns the current (possibly empty) list.
     *
     * @param rawKeys The keys returned by the store
     * @param user The user requesting the keys, required to have RANGE role for all keys returned
     * @param aclMap The ACL Map
     * @return
     * @throws KVSecurityException
     */
    public static List<KVKey> filterRawKeysToAuthorizedKeys(Iterable<KVKey> rawKeys, long user, Map<Long, Command.Security.ACL> aclMap) throws KVSecurityException {
        List<KVKey> rangeAllowedKeys = Lists.newArrayList();

        for (KVKey key : rawKeys) {
            LOG.fine("Checking RANGE permission on key <" + key + "> for user <" + user + "> ");

            if (Authorizer.hasPermission(aclMap, user,
                    Command.Security.ACL.Permission.RANGE, key.toByteString())) {
                LOG.fine("Permission found");
                rangeAllowedKeys.add(key);
            } else {
                // Short-circuit here, at the first disallowed key.
                LOG.fine("No permission found, stopping RANGE permission check here.");
                return rangeAllowedKeys;
            }
        }

        return rangeAllowedKeys;
    }
    
  private static void checkMaxKeyRange (int len) throws InvalidRequestException {
  if (len > maxRangeSize) {
      throw new InvalidRequestException ("request key range exceeds allowed size " + maxRangeSize + ", request size = " + len);
  }
}

}
