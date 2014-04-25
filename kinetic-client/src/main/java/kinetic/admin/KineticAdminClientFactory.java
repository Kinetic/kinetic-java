/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
