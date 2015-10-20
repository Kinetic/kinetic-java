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
