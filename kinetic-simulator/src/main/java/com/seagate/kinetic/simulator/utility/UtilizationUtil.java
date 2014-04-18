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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.seagate.kinetic.proto.Kinetic.Message.GetLog.Utilization;

/**
 * 
 * UtilizationUtil
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */
public abstract class UtilizationUtil {
	public static List<Utilization> getUtilization() {
		return UtilizationGenerator.generate();
	}
}

class UtilizationGenerator {
	private static final Random random = new Random();

	public static List<Utilization> generate() {
		List<Utilization> utilizations = new ArrayList<Utilization>();

		Utilization utilization = null;

		utilization = Utilization.newBuilder().setName("HDA")
				.setValue(generateValue()).build();
		utilizations.add(utilization);

		utilization = Utilization.newBuilder().setName("EN0")
				.setValue(generateValue()).build();
		utilizations.add(utilization);
		
		utilization = Utilization.newBuilder().setName("EN1")
				.setValue(generateValue()).build();
		utilizations.add(utilization);

		utilization = Utilization.newBuilder().setName("CPU")
				.setValue(generateValue()).build();
		utilizations.add(utilization);

		return utilizations;
	}

	private static float generateValue() {
		float utility = random.nextFloat();
		DecimalFormat df = new DecimalFormat("########.00");
		double remainValue = Double.parseDouble(df.format(utility));
		return (float) remainValue;

	}
}
