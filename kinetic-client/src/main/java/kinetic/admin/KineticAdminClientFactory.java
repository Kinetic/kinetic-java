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

import kinetic.client.KineticException;

import com.seagate.kinetic.admin.impl.DefaultAdminClient;

/**
 * 
 * Kinetic admin client factory provides a factory method to construct new
 * instances of <code>KineticAdminClient</code>.
 * 
 * @author Chiaming Yang
 * @author Chenchong(Emma) Li
 * 
 */
public class KineticAdminClientFactory {

    /**
     * Construct a new instance of the <code>KineticAdminClient</code>.
     * 
     * @param config
     *            configuration used to create a new instance of admin client.
     * @return a KineticAdminClient instance that is connected to the server.
     * 
     * @throws KineticException
     *             if any internal errors occur.
     */
    public static KineticAdminClient createInstance(
            AdminClientConfiguration config) throws KineticException {
        return new DefaultAdminClient(config);
    }
}
