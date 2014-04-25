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
