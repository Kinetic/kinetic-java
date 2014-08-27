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
 * <p>
 * All administrative operations by default use SSL connections to connect to the Drive or Simulator. 
 * And this is the only supported transport for all administrative operations.
 * <p>
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
     * 
     * Get the vendor specific log message.
     * <p>
     * The Device GetLog message is to ask the device to send back the
     * log of a certain name in the value field. The limit of each
     * log is 1m byte.
     * <p>
     * Proprietary names should be prefaced by the vendor name so that name
     * collisions do not happen in the future. An example could be names that
     * start with “com.wd” would be for Western Digital devices.
     * <p>
     * If the name is not found, the EntryNotFoundException is thrown.
     * <p>
     * There can be only one Device in the list of logs that can be retrieved.
     * 
     * @throws EntryNotFoundException if unable to get the log entry for the specified name.
     * 
     * @throws KineticException if any internal errors occur.
     * 
     * @param name the vendor specific name for the getLog command.
     * 
     *  @return <code>Device</code> that contains the name and value for the getLog command.
     */
    public Device getVendorSpecificDeviceLog (byte[] name) throws KineticException;

    /**
     * Set the access control list to the Kinetic drive.
     * <p>
     * Lock Enable: Will be utilized to enable lock on power cycle. 
     * <p>
     * If the Client sets a non-empty value for the lock pin, device will be locked after a power cycle. 
     * Access to the data is not immediately impacted, but a subsequent power cycle will result in a locked drive.
     * <p>
     * Lock Disable: If the Client sets an empty value for the lock pin, lock enable feature is turned off. 
     * This provides the Client with a mechanism for turning off the locking feature after previously enabling it.
     * <p>
     * A device must be in unlock mode before a Security operation can be performed.
     * <p>
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
    public void setSecurity(List<ACL> acls, byte[] oldLockPin, byte[] newLockPin, byte[] oldErasePin, byte[] newErasePin) throws KineticException;

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
     *             
     * 
     */
    public void instantErase(byte[] pin) throws KineticException;
    
    /**
     * Securely erase all user data, configurations, and setup information on the 
     * drive.
     * 
     * @param pin the pin used to authenticate for this operation.
     * 
     * @throws KineticException if unable to perform the pin operation.
     * 
     * @see #setSecurity(List, byte[], byte[], byte[], byte[])
     */
    public void secureErase (byte[] pin) throws KineticException;
    
    /**
     * Lock the device with the specified pin.
     * <p>
     * If the Client has set a non-zero length locking pin (to enable locking), a subsequent call to lockDevice will
     * lock the device.  
     * 
     * @param pin the pin to authenticate to the service.
     *
     * @throws KineticException if any internal error occurred.
     * 
     * @see #setSecurity(List, byte[], byte[], byte[], byte[])
     */
    public void lockDevice (byte[] pin) throws KineticException;
    
    /**
     * Unlock the device with the specified pin.
     * <p>
     * A successful unLockDevice call will unlock the previous locked device.
     * 
     * @param pin the pin to authenticate to the service.
     * 
     * @throws KineticException if any internal error occurred.
     * 
     * @see #setSecurity(List, byte[], byte[], byte[], byte[])
     */
    public void unLockDevice (byte[] pin) throws KineticException;
    
    /**
     * Set cluster version with the specified version.
     * 
     * @param newClusterVersion
     * @throws KineticException
     */
    public void setClusterVersion (long newClusterVersion) throws KineticException;

    /**
     * Close the connection and release all resources allocated by this
     * instance.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     */
    public void close() throws KineticException;
}
