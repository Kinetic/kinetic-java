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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.compression.Snappy;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;

/**
 * Kinetic tag calculation with CRC32C checksum algorithm util.
 * 
 * @author chiaming
 *
 */
public class Crc32cTagCalc implements KineticTagCalc {

    private final static Logger logger = Logger.getLogger(Crc32cTagCalc.class
            .getName());

    // crc32c algo
    private static String myName = "CRC32c";

    public Crc32cTagCalc() {
        logger.info(myName + " checksum isntance instantiated ...");
    }

    @Override
    public ByteString calculateTag(byte[] value) {

        try {
            
            // netty io bytebuf
            ByteBuf bb = Unpooled.wrappedBuffer(value);
            
            // calculate crc32c checksum
            int cval = Snappy.calculateChecksum(bb);

            logger.info("****** cval = " + cval);

            // convert to byte[]
            byte[] checkSum = ByteBuffer.allocate(4).putInt(cval).array();

            // convert to bytestring and return
            return ByteString.copyFrom(checkSum);

        } finally {
            ;
        }
    }

    @Override
    public String getAlgoName() {
        return myName;
    }

}
