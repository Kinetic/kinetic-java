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

import java.text.DecimalFormat;
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
		float utility = random.nextFloat();
		DecimalFormat df = new DecimalFormat("########.00");
		double remainValue = Double.parseDouble(df.format(utility));
		return (float) remainValue;

	}
}
