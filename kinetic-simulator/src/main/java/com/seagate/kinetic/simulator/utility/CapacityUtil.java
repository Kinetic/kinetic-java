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
package com.seagate.kinetic.simulator.utility;

import java.io.File;

import java.util.logging.Logger;

import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Capacity;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;

/**
 *
 * CapacityUtil
 * <p>
 *
 * @author Chenchong(Emma) Li
 *
 */
public abstract class CapacityUtil {

    private final static Logger logger = Logger.getLogger(CapacityUtil.class
            .getName());

    //private static DecimalFormat format = new DecimalFormat("########.00");

    //private static long MB = 1000000;

    public static Capacity getCapacity(SimulatorEngine engine) {

        Capacity capacity = null;

        try {
            // get store path
            String storePath = engine.getPersistStorePath();
        	
            File file = new File(storePath);

            long total = file.getTotalSpace();

            float remaining = (float) file.getFreeSpace();
            
            float portionFull = (total - remaining)/total;

            capacity = Capacity.newBuilder().setNominalCapacityInBytes(total)
                    .setPortionFull(portionFull).build();

        } catch (Exception e) {

            logger.warning("unable to obtain disk capacity, using generated numbers ...");

            capacity = CapacityGenerator.generate();
        }

        return capacity;
    }
}

class CapacityGenerator {
    //private static final Random random = new Random();
    private static final long TB = 1024 * 1024 * 1024 * 1024; // Unit: bytes

    public static Capacity generate() {
        long total = 4 * TB;
        float remaining = (float) 0.5;

        Capacity capacity = null;
        capacity = Capacity.newBuilder().setNominalCapacityInBytes((total))
                .setPortionFull(remaining).build();

        return capacity;
    }
}
