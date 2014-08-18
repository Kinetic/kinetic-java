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
package com.seagate.kinetic.simulator.lib;

import java.security.Key;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.crypto.spec.SecretKeySpec;

import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.HMACAlgorithmUtil;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.HMACAlgorithm;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope;

public class HmacStore {

    private final static Logger logger = Logger.getLogger(HmacStore.class
            .getName());

    private static final Long DEMO_USER = Long.valueOf(1);

    // key used for demo.
    private static final String DEMO_KEY = "asdfasdf";

    private static final HMACAlgorithm DEFAULT_ALGO = HMACAlgorithm.HmacSHA1;

    /**
     * XXX 06/28/2013 chiaming: fix this
     */
    static Hashtable<Long, Key> users = null;
    static {
        users = new Hashtable<Long, Key>();
        users.put((long) 1, new SecretKeySpec(ByteString.copyFromUtf8(DEMO_KEY)
                .toByteArray(), DEFAULT_ALGO.toString()));
    }

    /**
     * Get Hmac key map from Hmac store - prototype only
     *
     * @return Hmac map.
     */
    public static Map<Long, Key> getHmacKeyMap(Properties config) {

        HashMap<Long, Key> keyMap = new HashMap<Long, Key>();

        Key key = new SecretKeySpec(ByteString.copyFromUtf8(DEMO_KEY)
                .toByteArray(), DEFAULT_ALGO.toString());

        keyMap.put(DEMO_USER, key);

        return keyMap;
    }

    /**
     * Get Hmac key map from aclmap. Only support HmacSHA1 at this time.
     *
     * @return Hmac map.
     * @throws KineticException
     */
    public static Map<Long, Key> getHmacKeyMap(Map<Long, ACL> aclmap)
            throws KineticException {

        HashMap<Long, Key> keyMap = new HashMap<Long, Key>();

        // // XXX: always use default (HmacSHA1) at this time.
        // String algoName = HMACAlgorithm.HmacSHA1.toString();

        String algoName;
        for (ACL acl : aclmap.values()) {

            if (!acl.hasHmacAlgorithm()
                    || !HMACAlgorithmUtil.isSupported(acl.getHmacAlgorithm())) {
                throw new KineticException("No such HMAC algorithm : "
                        + acl.getHmacAlgorithm().toString());
            } else {
                algoName = acl.getHmacAlgorithm().toString();
            }

            logger.info("creating key for user=" + acl.getIdentity() + ", key="
                    + acl.getKey().toStringUtf8() + ", algo=" + algoName);

            // construct key
            Key key = new SecretKeySpec(acl.getKey().toByteArray(), algoName);

            // add to map
            keyMap.put(acl.getIdentity(), key);
        }

        return keyMap;
    }

    /**
     *
     * build server acl map instance.
     *
     * XXX 07092013 chiaming: prototype only.
     *
     * @return acl map instance.
     */
    public static Map<Long, ACL> getAclMap() {

        Map<Long, ACL> aclmap = new HashMap<Long, ACL>();

        ACL.Builder aclBuilder = ACL.newBuilder();

        aclBuilder.setIdentity(DEMO_USER);
        aclBuilder.setKey(ByteString.copyFromUtf8(DEMO_KEY));
        aclBuilder.setHmacAlgorithm(DEFAULT_ALGO);

        Scope.Builder scope = Scope.newBuilder();
        // add all roles except Role.INVALID
        for (Permission role : Permission.values()) {
            if (!role.equals(Permission.INVALID_PERMISSION)) {
                scope.addPermission(role);
            }
        }

        // add domains.
        aclBuilder.addScope(scope);

        ACL acl = aclBuilder.build();

        aclmap.put(DEMO_USER, acl);

        return aclmap;
    }
}
