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
