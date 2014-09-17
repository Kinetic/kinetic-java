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
package com.seagate.kinetic.simulator.internal;

import java.util.Map;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope;

/**
 * Prototype.
 *
 * @author chiaming
 *
 */
public class Authorizer {

    private final static Logger logger = Logger.getLogger(Authorizer.class
            .getName());

    /**
     * Check permission.
     *
     * @param aclmap
     *            userId/ACL map
     * @param user
     *            userId associated with its Hmac key.
     * @param role
     *            operation request role (command).
     */
    public static void checkPermission(Map<Long, ACL> aclmap, long user,
            Permission role) throws KVSecurityException {

        // check if there is an ACL entry for the userId.
        ACL acl = aclmap.get(user);

        if (acl == null) {
            throw new KVSecurityException("permission denied.");
        }

        // chiaming: fix this - only support one domain
        for (Scope domain : acl.getScopeList()) {
            // check if the request has the role (permission) to perform the op
            if (domain.getPermissionList().contains(role) == false) {
                throw new KVSecurityException("permission denied.");
            }
        }

        logger.fine("check operation passed: " + role);

        return;
    }

    /**
     * Check permission.
     *
     * @param aclmap
     *            userId/ACL map
     * @param user
     *            userId associated with its Hmac key.
     * @param role
     *            operation request role (command).
     * @param key
     *            from the request.
     */
    public static void checkPermission(Map<Long, ACL> aclmap, long user,
            Permission role, ByteString key) throws KVSecurityException {

        if (!hasPermission(aclmap, user, role, key)) {
            throw new KVSecurityException("permission denied");
        }

        logger.fine("check operation passed: " + role);

        return;
    }

    /**
     * Returns true iff the user has the given role on the given key
     *
     * @param aclmap
     * @param user
     * @param role
     * @param key
     * @return
     * @throws KVSecurityException for unexpected configuration problems related to security parameters
     */
    public static boolean hasPermission(Map<Long, ACL> aclmap, long user,
            Permission role, ByteString key) throws KVSecurityException {
        if (null == key) {
            throw new KVSecurityException(
                    "permission denied. the parameter of key is invalid.");
        }

        // check if there is an ACL entry for the userId.
        ACL acl = aclmap.get(user);

        if (acl == null) {
            throw new KVSecurityException("permission denied. ACL is null");
        }

        // chiaming: fix this - only support one domain
        // 7/20/13 Emma modify to support domains
        boolean hasRightRole = false;
        for (Scope domain : acl.getScopeList()) {
            long offset = domain.getOffset();
            if (0 > offset) {
                throw new KVSecurityException(
                        "permission denied. domain offset is invalid.");
            }

            ByteString currentDomainValue = domain.getValue();
            if (currentDomainValue.size() > key.size()) {
                throw new KVSecurityException(
                        "permission denied. domain value size is bigger than key size.");
            }

            int startValueIndex = (int) offset;
            int endValueIndex = startValueIndex + currentDomainValue.size();
            ByteString domainValueFromKey = key.substring(startValueIndex,
                    endValueIndex);

            if (equals(currentDomainValue.toByteArray(),
                    domainValueFromKey.toByteArray())) {
                if (domain.getPermissionList().contains(role) == true) {
                    hasRightRole = true;
                    break;
                }
            }
        }
        return hasRightRole;
    }

    private static boolean equals(byte[] byteArray1, byte[] byteArray2) {
        if (null == byteArray1 && null == byteArray2) {
            return true;
        } else if (null == byteArray1 || null == byteArray2) {
            return false;
        } else {
            if (byteArray1.length != byteArray2.length) {
                return false;
            } else {
                for (int i = 0; i < byteArray1.length; i++) {
                    if (byteArray1[i] != byteArray2[i]) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
