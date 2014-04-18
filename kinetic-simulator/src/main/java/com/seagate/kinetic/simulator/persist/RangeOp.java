// Do NOT modify or remove this copyright and confidentiality notice!
//
// Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
//
// The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
// Portions are also trade secret. Any use, duplication, derivation, distribution
// or disclosure of this code, for any reason, not expressly authorized is
// prohibited. All other rights are expressly reserved by Seagate Technology, LLC.

package com.seagate.kinetic.simulator.persist;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.Hmac;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message.Range;
import com.seagate.kinetic.proto.Kinetic.Message.Status;
import com.seagate.kinetic.simulator.internal.Authorizer;
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
            Message request, Message.Builder respond, Map<Long, Message.Security.ACL> aclMap) {

        ByteString k1 = null, k2 = null;
        boolean i1, i2, reverse;
        int n;

        try {
            try {

                // XXX need to add support for multiple ranges in getKeyRange
                Range r = request.getCommand().getBody().getRange();

                k1 = r.getStartKey();

                LOG.fine("k1: " + Hmac.toString(k1));

                k2 = r.getEndKey();

                LOG.fine("k2: " + Hmac.toString(k2));

                i1 = r.getStartKeyInclusive();

                i2 = r.getEndKeyInclusive();
                n = r.getMaxReturned();

                reverse = r.getReverse();

                if (n < 1) {
                    oops("the number of entries is <= 0");
                }

                List<KVKey> kvKeys = null;

                switch (request.getCommand().getHeader().getMessageType()) {
                case GETKEYRANGE:
                    if (reverse) {
                        List<KVKey> l = (ArrayList<KVKey>) store.getRangeReversed(
                                k1, i1, k2, i2, n);
                        LOG.fine("getKeyRangeReversed returned " + l.size() + " entries");

                        kvKeys = filterRawKeysToAuthorizedKeys(l, request
                                .getCommand().getHeader().getIdentity(), aclMap);
                    } else {
                        SortedMap<KVKey, KVValue> m = (SortedMap<KVKey, KVValue>) store
                                .getRange(k1, i1, k2, i2, n);
                        LOG.fine("getKeyRange returned " + m.size() + " entries");

                        kvKeys = filterRawKeysToAuthorizedKeys(m.keySet(),
                                request.getCommand().getHeader().getIdentity(),
                                aclMap);
                    }
                    break;
                default:
                    oops("Unknown request");
                }

                for (KVKey kvKey : kvKeys) {
                    LOG.fine("key="
                            + Hmac.toString(kvKey.toByteString()));

                    respond.getCommandBuilder().getBodyBuilder()
                    .getRangeBuilder()
                    .addKey(kvKey.toByteString());

                }

                // set ack sequence
                respond.getCommandBuilder()
                .getHeaderBuilder()
                .setAckSequence(
                        request.getCommand().getHeader().getSequence());

                // set status
                respond.getCommandBuilder().getStatusBuilder()
                .setCode(Status.StatusCode.SUCCESS);

                // TODO check multi-tenant key prefix
            } catch (Exception e) {
                LOG.fine(e.toString());
                Writer writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                LOG.fine(writer.toString());

                oops(Status.StatusCode.INTERNAL_ERROR,
                        "Opps2: " + e.getMessage() + "--" + e.toString());
            }
        } catch (RangeException e) {
            respond.getCommandBuilder().getStatusBuilder().setCode(e.status);
            respond.getCommandBuilder().getStatusBuilder()
            .setStatusMessage(e.getMessage());
        } finally {

            switch (request.getCommand().getHeader().getMessageType()) {
            case GETKEYRANGE:
                respond.getCommandBuilder().getHeaderBuilder()
                .setMessageType(MessageType.GETKEYRANGE_RESPONSE);
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
    public static List<KVKey> filterRawKeysToAuthorizedKeys(Iterable<KVKey> rawKeys, long user, Map<Long, Message.Security.ACL> aclMap) throws KVSecurityException {
        List<KVKey> rangeAllowedKeys = Lists.newArrayList();

        for (KVKey key : rawKeys) {
            LOG.fine("Checking RANGE permission on key <" + key + "> for user <" + user + "> ");

            if (Authorizer.hasPermission(aclMap, user,
                    Message.Security.ACL.Permission.RANGE, key.toByteString())) {
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

}
