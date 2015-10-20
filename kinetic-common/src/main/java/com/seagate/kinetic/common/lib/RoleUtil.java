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
package com.seagate.kinetic.common.lib;

import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;

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
