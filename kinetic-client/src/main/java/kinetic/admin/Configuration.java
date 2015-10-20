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
