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

import java.util.Arrays;
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

        /**
         * check if key is within the defined scope
         */
        boolean hasRightRole = false;
        for (Scope scope : acl.getScopeList()) {
            long offset = scope.getOffset();
            if (0 > offset) {
                throw new KVSecurityException(
                        "permission denied. domain offset is invalid.");
            }

            ByteString currentScopeValue = scope.getValue();
            if (currentScopeValue.size() > key.size()) {
                throw new KVSecurityException(
                        "permission denied. domain value size is bigger than key size.");
            }

            int startValueIndex = (int) offset;
            int endValueIndex = startValueIndex + currentScopeValue.size();
            ByteString scopeValueFromKey = key.substring(startValueIndex,
                    endValueIndex);

            if (equals(currentScopeValue.toByteArray(),
                    scopeValueFromKey.toByteArray())) {
                if (scope.getPermissionList().contains(role) == true) {
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

    /**
     * Returns true if the user has the given role on the given key range.
     * 
     * 1. Start key and end key must be with in the same scope and has Range
     * role for the scope. The start key and end key must have the same prefix:
     * 
     * The byte array value of (0, offset+scopeValue.size()) must be equal for
     * start and end keys.
     * 
     * 2. See the protocol definition for ACL for boundary scenarios.
     * 
     * https://github.com/Seagate/kinetic-protocol/blob/master/kinetic.proto
     * 
     *
     * @param aclmap
     * @param user
     * @param role
     * @param startKey
     * @return
     * @throws KVSecurityException
     *             for unexpected configuration problems related to security
     *             parameters
     */
    public static boolean hasRangePermission(Map<Long, ACL> aclmap, long user,
            Permission role, ByteString startKey, ByteString endKey)
            throws KVSecurityException {

        if (null == startKey || null == endKey) {
            throw new KVSecurityException(
                    "permission denied. start key and end key cannot be null");
        }

        // check if there is an ACL entry for the userId.
        ACL acl = aclmap.get(user);

        if (acl == null) {
            throw new KVSecurityException("permission denied. ACL is null");
        }

        /**
         * check if key is within the defined scope
         */
        boolean hasRightRole = false;
        for (Scope scope : acl.getScopeList()) {
            long offset = scope.getOffset();
            if (0 > offset) {
                throw new KVSecurityException(
                        "permission denied. domain offset is invalid.");
            }

            ByteString currentScopeValue = scope.getValue();
            if (currentScopeValue.size() > startKey.size()) {
                throw new KVSecurityException(
                        "permission denied. domain value size is bigger than key size.");
            }

            int startValueIndex = (int) offset;
            int endValueIndex = startValueIndex + currentScopeValue.size();

            // start key scope
            ByteString scopeValueFromStartKey = startKey.substring(
                    startValueIndex, endValueIndex);

            byte[] currentScope = currentScopeValue.toByteArray();

            if (equals(currentScope, scopeValueFromStartKey.toByteArray())) {

                if (scope.getPermissionList().contains(role) == true) {

                    /**
                     * at this point, start key is within the permitted scope.
                     * if no offset and vale is defined, the user has ALL range
                     * for the current scope.
                     */
                    if (offset == 0 && currentScopeValue.isEmpty()) {
                        return true;
                    }

                    /**
                     * if endkey == "", use max value for the end key.
                     */
                    if (endKey.isEmpty()) {
                        byte[] maxKey = new byte[4096];

                        Arrays.fill(maxKey, (byte) 0XFF);
                        endKey = ByteString.copyFrom(maxKey);
                    }

                    // end key scope value
                    ByteString scopeValueFromEndKey = endKey.substring(
                            startValueIndex, endValueIndex);

                    /**
                     * start and end key must be within the same scope and same
                     * prefix
                     */
                    if (equals(currentScope, scopeValueFromEndKey.toByteArray())) {

                        // start key prefix
                        ByteString startKeyRangeScope = startKey.substring(0,
                                endValueIndex);

                        // end key prefix
                        ByteString endKeyRangeScope = endKey.substring(0,
                                endValueIndex);

                        // compare start and end key's (prefix + scope)
                        if (equals(startKeyRangeScope.toByteArray(),
                                endKeyRangeScope.toByteArray())) {
                            hasRightRole = true;
                            break;
                        }
                    }

                }
            }
        }
        return hasRightRole;
    }
}
