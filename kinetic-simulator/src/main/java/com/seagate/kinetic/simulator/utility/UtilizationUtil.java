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

import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Utilization;

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
	    float utility = (float)random.nextInt(101) / 100;
		return utility;
	}
}
