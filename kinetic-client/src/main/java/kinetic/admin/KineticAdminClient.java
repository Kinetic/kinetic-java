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
 * Kinetic administrative client interface.
 * <p>
 * Kinetic administrators use this interface to setup kinetic drives. such as
 * set up access control list for the drives.
 * 
 * @author James Hughes.
 * @author Chiaming Yang
 * @author Chenchong Li
 */
public interface KineticAdminClient {

    /**
     * Setup the Kinetic drive.
     * 
     * @param pin
     *            Compare the pin with drive's pin. If equal, can setup the
     *            information for the drive, if not, drive will reject the setup
     *            request.
     * 
     * @param setPin
     *            new pin will replace the pin in the drive.
     * 
     * @param newClusterVersion
     *            set the new cluster version for the drive.
     * 
     * @param secureErase
     *            If secureErase is set true, all data in database will be
     *            deleted.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     */
    public void setup(byte[] pin, byte[] setPin, long newClusterVersion,
            boolean secureErase) throws KineticException;

    /**
     * Load firmware byte[] to the drive.
     * <p>
     * The firmware byte[] is itself protected on its own for integrity,
     * authenticity, etc
     * <p>
     * 
     * @param pin
     *            Compare the pin with drive's pin. If equal, can download
     *            firmware the information for the drive, if not, drive will
     *            reject the firmwareDownload request.
     * 
     * @param bytes
     *            update firmware bytes for the drive.
     * 
     * @throws KineticException
     *             if unable to load firmware bytes to the drive.
     */
    public void firmwareDownload(byte[] pin, byte[] bytes)
            throws KineticException;

    /**
     * Get all Kinetic logs, such as the utilization temperature and capacity
     * information from the drive.
     * <p>
     * 
     * @return All the KineticLog Log information obtained from the Kinetic
     *         drive, including <code>Utilization</code>,
     *         <code>Temperature</code>, and <code>Capacity</code>, etc.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see KineticLog
     * @see Utilization
     * @see Temperature
     * @see Capacity
     * 
     */
    public KineticLog getLog() throws KineticException;

    /**
     * 
     * Get a subset of specific Kinetic log information based on the log types
     * specified in the parameter.
     * <p>
     * The list of KineticLogType must not be empty, otherwise a
     * KineticException is thrown.
     * 
     * @param listOfLogType
     *            a subset of Kinetic log information to be returned.
     * 
     * @return a subset of Kinetic log information based on the log types
     *         specified in the parameter.
     * 
     * @throws KineticException
     *             if any internal errors occurred.
     */
    public KineticLog getLog(List<KineticLogType> listOfLogType)
            throws KineticException;

    /**
     * Set the access control list to the Kinetic drive.
     * 
     * @param acls
     *            ACL information needs to be set.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * @see ACL
     * @see Domain
     * @see Role
     */
    public void setSecurity(List<ACL> acls) throws KineticException;

    /**
     * Erase all data in database for the drive.
     * <p>
     * Erase data in database with Db API.
     * <p>
     * 
     * @param pin
     *            Compare the pin with drive's pin. If equal, can download
     *            firmware the information for the drive, if not, drive will
     *            reject the firmwareDownload request.
     * 
     * @throws KineticException
     *             if unable to load firmware bytes to the drive.
     */
    public void instantErase(byte[] pin) throws KineticException;

    /**
     * Close the connection and release all resources allocated by this
     * instance.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     */
    public void close() throws KineticException;
}
