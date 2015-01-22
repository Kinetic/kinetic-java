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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.proto.Kinetic.Command.Algorithm;

/**
 * 
 * @author chiaming
 *
 */
public class MessageDigestUtil {

    private final static Logger logger = Logger
            .getLogger(MessageDigestUtil.class.getName());

    public static ByteString calculateTag(Algorithm algo, byte[] value) {

        byte[] digest = null;

        // get md instance
        MessageDigest md = getInstance(algo);

        if (value == null) {
            value = new byte[0];
        }

        // calculate and return digest
        digest = md.digest(value);

        return ByteString.copyFrom(digest);

    }

    public static boolean isSupportedForKineticJava(Algorithm algo) {

        switch (algo) {
        case SHA1:
            return true;
        case SHA2:
            return true;
        default:
            return false;
        }
    }

    public static MessageDigest getInstance(Algorithm algo) {

        switch (algo) {
        case SHA1:
            return getDigestInstance("SHA-1");
        case SHA2:
            return getDigestInstance("SHA-256");
        default:
            throw new java.lang.UnsupportedOperationException(
                    "unsupported algorithm., name = " + algo.name());
        }
    }

    public static MessageDigest getDigestInstance(String algoName) {

        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance(algoName);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        return md;
    }

}
