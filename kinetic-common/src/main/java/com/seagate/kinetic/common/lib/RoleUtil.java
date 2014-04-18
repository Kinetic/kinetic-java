/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.common.lib;

import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Permission;

/**
 * Get Role list from proto and give the method to judge the role if valid.
 *
 */
public abstract class RoleUtil {

    private static Permission[] roleOfArray = Permission.values();

    // comment out un-used code
    // public static boolean isValid(String role) throws KineticException {
    //
    // boolean result = false;
    //
    // try {
    // Role roleOfEnum = Role.valueOf(role);
    //
    // result = isValid(roleOfEnum);
    //
    // } catch (Exception e) {
    // throw new KineticException(e.getMessage());
    // }
    //
    // return result;
    // }

    public static boolean isValid(Permission role) {

        for (Permission roleOfProto : roleOfArray) {
            if (roleOfProto == role && Permission.INVALID_PERMISSION != role) {
                return true;
            }
        }

        return false;
    }
}
