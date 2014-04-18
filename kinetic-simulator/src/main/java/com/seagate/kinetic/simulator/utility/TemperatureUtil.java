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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.seagate.kinetic.proto.Kinetic.Message.GetLog.Temperature;

/**
 * 
 * TemperatureUtil
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */
public abstract class TemperatureUtil {
	public static List<Temperature> getTemperature() {
		return TemperatureGenerator.generate();
	}
}

class TemperatureGenerator {
	private static final Random random = new Random();
	private static final float MAX = 100;
	private static final float MIN = 5;
	private static final float TARGET = 25;

	public static List<Temperature> generate() {
		List<Temperature> temperatures = new ArrayList<Temperature>();

		Temperature temperature = null;

		temperature = Temperature.newBuilder().setName("HDA").setMaximum(MAX)
				.setMinimum(MIN).setTarget(TARGET)
				.setCurrent(generateCurrent()).build();
		temperatures.add(temperature);
		
		temperature = Temperature.newBuilder().setName("CPU").setMaximum(MAX)
				.setMinimum(MIN).setTarget(TARGET)
				.setCurrent(generateCurrent()).build();
		temperatures.add(temperature);

		return temperatures;
	}

	private static float generateCurrent() {
		return MIN + random.nextInt((int) (MAX - MIN));
	}
}
