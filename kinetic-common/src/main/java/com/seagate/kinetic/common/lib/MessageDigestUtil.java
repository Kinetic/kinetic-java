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

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

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

    // SHA1 tag calc instance
    private static KineticTagCalc sha1Algo = null;

    // SHA2 tag calc instance
    private static KineticTagCalc sha2Algo = null;

    // CRC32 checksum instance
    private static KineticTagCalc crc32 = null;
    
    // CRC32C checksum instance
    private static KineticTagCalc crc32c = null;

    public static ByteString calculateTag(Algorithm algo, byte[] value) {

        ByteString tag = null;

        // get md instance
        KineticTagCalc tagCalc = getInstance(algo);

        if (value == null) {
            value = new byte[0];
        }

        // calculate and return digest
        tag = tagCalc.calculateTag(value);

        return tag;
    }

    public static boolean isSupportedForKineticJava(Algorithm algo) {

        switch (algo) {
        case SHA1:
        case SHA2:
        case CRC32:
        case CRC32C:
            return true;
        default:
            return false;
        }
    }

    /**
     * Get SHA1 instance.
     * 
     * @return SHA1 instance.
     */
    public static KineticTagCalc getSha1Instance() {

        // check if constructed
        if (sha1Algo != null) {
            return sha1Algo;
        }

        // sync
        synchronized (MessageDigestUtil.class) {

            // check if alread constructed
            if (sha1Algo == null) {
                sha1Algo = new MessageDigestTagCalc("SHA-1");
            }
        }

        return sha1Algo;
    }

    /**
     * Get SHA2 instance.
     * 
     * @return SHA2 instance.
     */
    public static KineticTagCalc getSha2Instance() {

        // check if constructed
        if (sha2Algo != null) {
            return sha2Algo;
        }

        // sync
        synchronized (MessageDigestUtil.class) {

            // check if already constructed
            if (sha2Algo == null) {
                sha2Algo = new MessageDigestTagCalc("SHA-256");
            }
        }

        return sha2Algo;
    }

    /**
     * Get CRC32 instance.
     * 
     * @return CRC32 instance.
     */
    public static KineticTagCalc getCrc32Instance() {

        // check if constructed
        if (crc32 != null) {
            return crc32;
        }

        // sync
        synchronized (MessageDigestUtil.class) {

            // check if already constructed
            if (crc32 == null) {
                crc32 = new Crc32TagCalc();
            }
        }

        return crc32;
    }

    /**
     * Get CRC32C instance.
     * 
     * @return CRC32C instance.
     */
    public static KineticTagCalc getCrc32cInstance() {

        // check if constructed
        if (crc32c != null) {
            return crc32c;
        }

        // sync
        synchronized (MessageDigestUtil.class) {

            // check if already constructed
            if (crc32c == null) {
                crc32c = new Crc32cTagCalc2();
            }
        }

        return crc32c;
    }


    public static KineticTagCalc getInstance(Algorithm algo) {

        switch (algo) {
        case SHA1:
            return getSha1Instance();
        case SHA2:
            return getSha2Instance();
        case CRC32:
            return getCrc32Instance();
        case CRC32C:
        	return getCrc32cInstance();
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

    public static Checksum getCrc32ChecksumInstance() {
        return new CRC32();
    }

    public ByteString calculateTag(Checksum crc, byte[] value) {

        crc.update(value, 0, value.length);

        long csum = crc.getValue();

        ByteBuffer buffer = ByteBuffer.allocate(8);

        return ByteString.copyFrom(buffer.putLong(csum).array());
    }

}
