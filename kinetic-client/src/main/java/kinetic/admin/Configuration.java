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

/**
 * Kinetic device configuration information.
 * 
 * @author Emma Li
 * @author chiaming
 *
 */
public interface Configuration {

    /**
     * Get vendor name.
     * 
     * @return vendor name
     */
    public String getVendor();

    /**
     * Get device model.
     * 
     * @return get device model.
     */
    public String getModel();

    /**
     * Get serial number.
     * 
     * @return serila number.
     */
    public String getSerialNumber();

    /**
     * Get device version.
     * 
     * @return device version.
     */
    public String getVersion();

    /**
     * Get device service code compilation date.
     * 
     * @return device service code compilation date.
     */
    public String getCompilationDate();

    /**
     * Get device service source code repository hash value.
     * 
     * @return device service source code repository hash value.
     */
    public String getSourceHash();

    /**
     * Get device interfaces as list.
     * 
     * @return device interfaces as list.
     */
    public List<Interface> getInterfaces();

    /**
     * Get device supported protocol version.
     * 
     * @return device supported protocol version.
     */
    public String getProtocolVersion();

    /**
     * Get supported protocol compilation date.
     * 
     * @return supported protocol compilation date.
     */
    public String getProtocolCompilationDate();

    /**
     * Get supported protocol source code repository hash value.
     * 
     * @return supported protocol source code repository hash value.
     */
    public String getProtocolSourceHash();

    /**
     * Get service port.
     * 
     * @return service port.
     */
    public int getPort();

    /**
     * Get TLS service port.
     * 
     * @return TLS service port.
     */
    public int getTlsPort();

    /**
     * Get device world wide name.
     * 
     * @return device world wide name.
     */
    public String getWorldWideName();
}
