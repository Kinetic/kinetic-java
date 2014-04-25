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

}
