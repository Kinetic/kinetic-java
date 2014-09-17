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

import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.HMACAlgorithm;

/**
 * Get HMAC Algorithm list from proto and give the method to judge the
 * HmacAlgorithm if supported..
 *
 */
public abstract class HMACAlgorithmUtil {

    private static HMACAlgorithm[] algoOfArray = HMACAlgorithm.values();

    public static boolean isSupported(String algorithm)
            throws UnsupportedOperationException {

        boolean result = false;

        try {
            HMACAlgorithm algo = HMACAlgorithm.valueOf(algorithm);

            result = isSupported(algo);

        } catch (Exception e) {
            // throw new KineticException(e.getMessage());
            throw new java.lang.UnsupportedOperationException(e);
        }

        return result;
    }

    public static boolean isSupported(HMACAlgorithm algorithm) {

        for (HMACAlgorithm hmacAlgorithm : algoOfArray) {
            if (hmacAlgorithm == algorithm
                    && HMACAlgorithm.INVALID_HMAC_ALGORITHM != algorithm) {
                return true;
            }
        }

        return false;
    }
}
