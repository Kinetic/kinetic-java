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
 * The Device GetLog message is to ask the device to send back the
 * log of a certain name in the value field. The limit of each
 * log is 1m byte.
 * <p>
 * Proprietary names should be prefaced by the vendor name so that name
 * collisions do not happen in the future. An example could be names that
 * start with “com.wd” would be for Western Digital devices.
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
     * Set the vendor specifc device name.
     *  
     * @param name the vendor specifc device name.
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
