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
