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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Temperature;

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
