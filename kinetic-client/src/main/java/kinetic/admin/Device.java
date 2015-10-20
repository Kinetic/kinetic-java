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

/**
 * 
 * The Device GetLog message is to ask the device to send back the
 * log of a certain name in the value field. The limit of each
 * log is 1m byte.
 * <p>
 * Proprietary names should be prefaced by the vendor name so that name
 * collisions do not happen in the future. An example could be names that
 * start with "com.wd" would be for Western Digital devices.
 * <p>
 * If the name is not found, the get log returns NOT_FOUND.
 * <p>
 * There can be only one Device in the list of logs that can be retrieved.
 * 
 * @author chiaming
 *
 */
public class Device {
    
    // name of the vendor specific log
    private byte[] name = null;
    
    // value of the vendor specific log.
    private byte[] value = null;
    
    /**
     * default constructor.
     */
    public Device() {
        ;
    }
    
    /**
     * Get the vendor specific device log name.
     * 
     * @return the vendor specific device log name
     */
    public byte[] getName() {
        return this.name;
    }
    
    /**
     * Set the vendor specific device name.
     *  
     * @param name the vendor specific device name.
     */
    
    public void setName (byte[] name) {
        this.name = name;
    }
    
    /**
     * The vendor specific device value.
     * 
     * @param value vendor specific device value associated with the specified name.
     * 
     * @see #setName(byte[])
     */
    public void setValue (byte[] value) {
        this.value = value;
    }
    
    /**
     * Get the vendor specific value.
     * 
     * @return the vendor specific value associated with the specified name.
     * 
     * @see #getName()
     */
    public byte[] getValue() {
        return this.value;
    }
    
}
