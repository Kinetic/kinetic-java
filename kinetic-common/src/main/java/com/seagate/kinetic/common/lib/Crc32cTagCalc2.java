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
