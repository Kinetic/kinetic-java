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

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.google.protobuf.ByteString;

/**
 * Kinetic tag calculation with CRC32 checksum algorithm utils.
 * 
 * @author chiaming
 *
 */
public class Crc32TagCalc implements KineticTagCalc {

    private final static Logger logger = Logger.getLogger(Crc32TagCalc.class
            .getName());

    private static String myName = "CRC32";

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
