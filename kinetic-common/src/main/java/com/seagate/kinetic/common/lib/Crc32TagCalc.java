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
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.google.protobuf.ByteString;

/**
 * Kinetic tag calculation with CRC32 checksum algorithm util.
 * 
 * @author chiaming
 *
 */
public class Crc32TagCalc implements KineticTagCalc {

    private final static Logger logger = Logger.getLogger(Crc32TagCalc.class
            .getName());

    // crc32 algo
    private static String myName = "CRC32";

    // crc32 instance
    private Checksum crc32 = null;

    public Crc32TagCalc() {

        logger.info(myName + " checksum isntance instantiated ...");

        crc32 = new CRC32();
    }

    @Override
    public synchronized ByteString calculateTag(byte[] value) {

        try {
            // do update
            crc32.update(value, 0, value.length);

            // get checksum value
            long cval = crc32.getValue();

            // convert to byte[]
            byte[] checkSum = ByteBuffer.allocate(8).putLong(cval).array();

            // convert to bytestring and return
            return ByteString.copyFrom(checkSum);

        } finally {
            // reset crc32 instance
            crc32.reset();
        }
    }

    @Override
    public String getAlgoName() {
        return myName;
    }

}
