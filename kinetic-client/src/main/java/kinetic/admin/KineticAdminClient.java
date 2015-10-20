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

import kinetic.client.EntryNotFoundException;
import kinetic.client.KineticException;
import kinetic.client.p2p.KineticP2pClient;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command.Priority;
import com.seagate.kinetic.proto.Kinetic.Command.Range;

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
public interface KineticAdminClient extends KineticP2pClient {

    /**
     * Load firmware byte[] to the drive.
     * <p>
     * The firmware byte[] is itself protected on its own for integrity,
     * authenticity, etc
     * <p>
     * 
     * @param pin
     *            No used. This is for backward compatibility only.
     * 
     * @param bytes
     *            update firmware bytes for the drive.
     * 
     * @throws KineticException
     *             if unable to load firmware bytes to the drive.
     * 
     * @deprecated
     * @see #firmwareDownload(byte[])
     */
    @Deprecated
    public void firmwareDownload(byte[] pin, byte[] bytes)
            throws KineticException;

    /**
     * Load firmware byte[] to the drive.
     * <p>
     * The firmware byte[] is itself protected on its own for integrity,
     * authenticity, etc
     * 
     * @param bytes
     *            update firmware bytes for the drive.
     * 
     * @throws KineticException
     *             if unable to load firmware bytes to the drive.
     */
    public void firmwareDownload(byte[] bytes) throws KineticException;

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
     * start with "com.wd" would be for Western Digital devices.
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
     * Set Security ACL list.
     * 
     * @param acls the ACL list to be set to drive/simulator.
     * 
     * @throws KineticException if any internal error occurred.
     */
    public void setAcl (List<ACL> acls) throws KineticException;
    
    /**
     * Set Security Lock pin.
     * 
     * @param oldLockPin old lock pin used to authenticate.
     * @param newLockPin the new lock pin to set to the kinetic drive/simulator.
     * 
     * @throws KineticException if any internal error occurred.
     */
    public void setLockPin (byte[] oldLockPin, byte[] newLockPin) throws KineticException;
    
    /**
     * 
     * @param oldErasePin old erase pin used to authenticate.
     * @param newErasePin new pin to set.
     * @throws KineticException if any internal error occurred.
     */
    public void setErasePin (byte[] oldErasePin, byte[] newErasePin) throws KineticException;
    
    /**
     * Erase all data in database for the drive. This maybe secure or not. This
     * operation implies that it maybe faster than the secured erase
     * alternative.
     * <p>
     * Please use {@link #secureErase(byte[])} if secured erase is desirable.
     * 
     * @param pin
     *            the pin used to authenticate for this operation.
     * 
     * @throws KineticException
     *             if unable to load firmware bytes to the drive.
     * 
     * @see #secureErase(byte[])
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
     * @see #setErasePin(byte[], byte[])
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
     * @see #setLockPin(byte[], byte[])
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
     * @see #lockDevice(byte[])
     */
    public void unLockDevice (byte[] pin) throws KineticException;
    
    /**
     * Set cluster version with the specified version.
     * 
     * @param newClusterVersion new cluster version to be set.
     * @throws KineticException if any internal error occurred
     */
    public void setClusterVersion (long newClusterVersion) throws KineticException;
    
    /**
     * Performs media scan operation to the Kinetic drive.
     * <p>
     * 
     * @param range range of background op
     * @param priority priority of background op
     * @return kinetic response message.
     * @throws KineticException if any internal error occurred
     * 
     */
    public KineticMessage mediaScan (Range range, Priority priority) throws KineticException;
    
    /**
     * Perform media optimize with the specified range and priority.
     * <p>
     * @param range range of the optimization
     * @param priority priority of this task
     * @return response message.
     * @throws KineticException if any internal error occurred.
     */
    public KineticMessage mediaOptimize(Range range, Priority priority) throws KineticException;
}
