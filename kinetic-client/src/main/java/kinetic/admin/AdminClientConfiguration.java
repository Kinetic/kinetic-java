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

import kinetic.client.ClientConfiguration;

/**
 * Kinetic admin Client configuration.
 * <p>
 * Kinetic admin applications construct a new instance of this instance and set
 * appropriate configurations. Application then calls
 * {@link KineticAdminClientFactory#createInstance(AdminClientConfiguration)} to
 * create a new instance of {@link KineticAdminClient}
 * </p>
 * 
 * @author James Hughes.
 * @author Chiaming Yang
 * 
 * @see KineticAdminClientFactory#createInstance(AdminClientConfiguration)
 * @see KineticAdminClient
 */
public class AdminClientConfiguration extends ClientConfiguration {

    private static final long serialVersionUID = 1194417884359507016L;

    /**
     * Admin Client configuration constructor.
     * 
     */
    public AdminClientConfiguration() {
        super.setUseSsl(true);
        setPort(getSSLDefaultPort());
    }
}
