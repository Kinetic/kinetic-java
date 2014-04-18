/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
        // return CapacityGenerator.generate();

        Capacity capacity = null;

        try {
            // XXX 10/18/2013 chiaming: use db file path
            File file = new File("/");

            float total = (float) Double.parseDouble(format.format(file
                    .getTotalSpace() / MB));

            float remaining = (float) Double.parseDouble(format.format(file
                    .getFreeSpace() / MB));

            capacity = Capacity.newBuilder().setTotal((long) total)
                    .setRemaining((long) remaining).build();

        } catch (Exception e) {

            logger.warning("unable to obtain disk capacity, using generated numbers ...");

            capacity = CapacityGenerator.generate();
        }

        return capacity;
    }
}

class CapacityGenerator {
    private static final Random random = new Random();
    private static final float TB = 1024 * 1024; // Unit: MB

    public static Capacity generate() {
        float total = 4 * TB;
        float remaining = total * random.nextFloat();
        DecimalFormat df = new DecimalFormat("########.00");
        double remainValue = Double.parseDouble(df.format(remaining));

        Capacity capacity = null;
        capacity = Capacity.newBuilder().setTotal((long) total)
                .setRemaining((long) remainValue).build();

        return capacity;
    }

}
