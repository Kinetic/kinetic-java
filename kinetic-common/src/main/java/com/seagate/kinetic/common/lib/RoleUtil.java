/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
