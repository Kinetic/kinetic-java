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

import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.HMACAlgorithm;

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
                    && HMACAlgorithm.Unknown != algorithm) {
                return true;
            }
        }

        return false;
    }
}