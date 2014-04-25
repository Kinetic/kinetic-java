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
