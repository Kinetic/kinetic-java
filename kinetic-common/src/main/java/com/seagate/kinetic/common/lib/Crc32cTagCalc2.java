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

import java.util.logging.Logger;

import com.google.cloud.Crc32c;
import com.google.protobuf.ByteString;

/**
 * Kinetic tag calculation with CRC32C checksum algorithm util.
 * 
 * @author chiaming
 *
 */
public class Crc32cTagCalc2 implements KineticTagCalc {

    private final static Logger logger = Logger.getLogger(Crc32cTagCalc2.class
            .getName());

    // crc32c algo
    private static String myName = "CRC32c";

    private Crc32c crc32c = new Crc32c();

    public Crc32cTagCalc2() {
        logger.info(myName + " checksum instance instantiated ...");
    }

    @Override
    public synchronized ByteString calculateTag(byte[] value) {

        try {
            
            // calculate crc32c checksum
            this.crc32c.update(value, 0, value.length);
            byte[] checkSum = this.crc32c.getValueAsBytes();

            // convert to bytestring and return
            return ByteString.copyFrom(checkSum);
        } finally {
            this.crc32c.reset();
        }
    }

    @Override
    public String getAlgoName() {
        return myName;
    }

}
