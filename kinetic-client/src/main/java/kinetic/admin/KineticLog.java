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
package kinetic.admin;

import java.util.List;

import kinetic.client.KineticException;

/**
 * 
 * Kinetic drive information log data container.
 * 
 * @see KineticAdminClient#getLog()
 */
public interface KineticLog {

	/**
	 * 
	 * Get the utilization information of the drive
	 * <p>
	 * 
	 * @return a List of utilization information from the drive.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @see Utilization
	 */
	public List<Utilization> getUtilization() throws KineticException;

	/**
	 * 
	 * Get the temperature information of the drive
	 * <p>
	 * 
	 * @return a List of temperature information from the drive.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @see Temperature
	 */
	public List<Temperature> getTemperature() throws KineticException;

	/**
	 * 
	 * Get the capacity information of the drive
	 * <p>
	 * 
	 * @return the capacity information from the drive.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @see Capacity
	 */
	public Capacity getCapacity() throws KineticException;

	/**
	 * 
	 * Get the configuration information of the drive
	 * <p>
	 * 
	 * @return the configuration information from the drive.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @see Configuration
	 */
	public Configuration getConfiguration() throws KineticException;

	/**
	 * 
	 * Get the statistics information of the drive
	 * <p>
	 * 
	 * @return a List of statistic information from the drive.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * @see Statistics
	 */
	public List<Statistics> getStatistics() throws KineticException;

	/**
	 * 
	 * Get Kinetic log messages.
	 * 
	 * @return Kinetic log messages from the drive.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public byte[] getMessages() throws KineticException;

	/**
	 * Get Kinetic log type values set in this log instance.
	 * 
	 * @return an array of KineticLogType information contains in this instance.
	 * 
	 * @throws KineticException
	 *             if any internal errors occur
	 */
	public KineticLogType[] getContainedLogTypes() throws KineticException;
	
	   /**
     * 
     * Get the limits information of the drive
     * <p>
     * 
     * @return the limits information from the drive.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @see Limits
     */
    public Limits getLimits() throws KineticException;

}
