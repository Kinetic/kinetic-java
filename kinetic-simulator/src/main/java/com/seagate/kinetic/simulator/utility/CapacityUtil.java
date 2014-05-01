/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.seagate.kinetic.simulator.utility;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.logging.Logger;

import com.seagate.kinetic.proto.Kinetic.Message.GetLog.Capacity;

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

    private static DecimalFormat format = new DecimalFormat("########.00");

    private static long MB = 1000000;

    public static Capacity getCapacity() {

        Capacity capacity = null;

        try {
            File file = new File("/");

            long total = (long)Double.parseDouble(format.format(file
                    .getTotalSpace() / MB));

            long remaining = (long) Double.parseDouble(format.format(file
                    .getFreeSpace() / MB));

            capacity = Capacity.newBuilder().setTotal(total)
                    .setRemaining(remaining).build();

        } catch (Exception e) {

            logger.warning("unable to obtain disk capacity, using generated numbers ...");

            capacity = CapacityGenerator.generate();
        }

        return capacity;
    }
}

class CapacityGenerator {
    private static final Random random = new Random();
    private static final long TB = 1024 * 1024; // Unit: MB

    public static Capacity generate() {
        long total = 4 * TB;
        long remaining = total * random.nextLong();

        Capacity capacity = null;
        capacity = Capacity.newBuilder().setTotal(total)
                .setRemaining(remaining).build();

        return capacity;
    }
}
