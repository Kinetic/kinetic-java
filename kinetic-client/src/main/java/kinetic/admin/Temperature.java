/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
