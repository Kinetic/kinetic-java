/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package kinetic.admin;

/**
 * 
 * Kinetic drive temperature information container.
 * 
 */
public class Temperature {
	// device name
	private String name;

	// the maximum temperature
	private float max;

	// the minimum temperature
	private float min;

	// the target temperature
	private float target;

	// the current temperature
	private float current;

	/**
	 * Get max name of the drive.
	 * 
	 * @return name of the drive
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of the drive.
	 * 
	 * @param name
	 *            name of the drive.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get max temperature of the drive.
	 * 
	 * @return max temperature of the drive
	 */
	public float getMax() {
		return max;
	}

	/**
	 * Set max temperature of the drive (set by the Kinetic drive).
	 * 
	 * @param max
	 *            max temperature of the drive.
	 */
	public void setMax(float max) {
		this.max = max;
	}

	/**
	 * Get minimum temperature of the drive.
	 * 
	 * @return minimum temperature of the drive
	 */
	public float getMin() {
		return min;
	}

	/**
	 * Set minimum temperature of the drive (set by the Kinetic drive).
	 * 
	 * @param min
	 *            min temperature of the drive.
	 */
	public void setMin(float min) {
		this.min = min;
	}

	/**
	 * Get target temperature of the drive.
	 * 
	 * @return target temperature of the drive.
	 */
	public float getTarget() {
		return target;
	}

	/**
	 * Set target temperature of the drive.
	 * 
	 * @param target
	 *            target temperature of the drive
	 */
	public void setTarget(float target) {
		this.target = target;
	}

	/**
	 * Get current temperature of the drive.
	 * 
	 * @return current temperature of the drive.
	 */
	public float getCurrent() {
		return current;
	}

	/**
	 * Set current temperature of the drive.
	 * 
	 * @param current
	 *            current temperature of the drive.
	 */
	public void setCurrent(float current) {
		this.current = current;
	}

}
