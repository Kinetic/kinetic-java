/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */

/**
 * Kinetic Java API.
 * <p>
 * A Kinetic application uses the Java API provided in this package to interface with
 * Kinetic services/servers.</P>
 * <p>
 * The boot-strap API for using the Kinetic client library is
 * <code>KineticClientFactory.createInstance(ClientConfiguration)</code>.</P>
 * <p>
 * Kinetic applications construct a new instance of <code>ClientConfiguration</code>
 * and set the appropriate configurations (such as server host/port) to the
 * configuration instance. Applications then invoke the static createInstance
 * method <code>KineticClientFactory.createInstance(ClientConfiguration)</code> to obtain a new
 * instance of KineticClient.</p>
 *
 */
package kinetic.client;

