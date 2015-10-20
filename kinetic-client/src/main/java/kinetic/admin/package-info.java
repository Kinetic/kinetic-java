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

/**
 * Kinetic Java Admin API.
 * <p>
 * A Kinetic admin user uses the Java Admin API provided in this package to configure, 
 * setup, and monitor Kinetic devices.</P>
 * <p>
 * The boot-strap API for using the Kinetic admin client library is
 * <code>KineticAdminClientFactory.createInstance(AdminClientConfiguration)</code>.</P>
 * <p>
 * Kinetic admin applications construct a new instance of <code>AdminClientConfiguration</code>
 * and set the appropriate configurations (such as server host/port) to the
 * configuration instance. Applications then invoke the static createInstance
 * method <code>KineticAdminClientFactory.createInstance(AdminClientConfiguration)</code> to obtain a new
 * instance of KineticAdminClient.</p>
 *
 */
package kinetic.admin;

