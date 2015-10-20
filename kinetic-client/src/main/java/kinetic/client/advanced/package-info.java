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
 * Kinetic Advanced Java Client API.
 * <p>
 * A Kinetic application uses the Advanced Java API provided in this package to interface with
 * Kinetic services with a rich set of options to perform operations such as PUT, GET, DELETE.
 * The advanced client also provides API for asynchronous K/V operations.</P>
 * <p>
 * The boot-strap API for using the Advanced Kinetic client library is
 * <code>AdvancedKineticClientFactory.createInstance(ClientConfiguration)</code>.</P>
 * <p>
 * Kinetic applications construct a new instance of <code>ClientConfiguration</code>
 * and set the appropriate configurations (such as server host/port) to the
 * configuration instance. Applications then invoke the static createInstance
 * method <code>AdvancedKineticClientFactory.createInstance(ClientConfiguration)</code> to obtain a new
 * instance of AdvancedKineticClient.</p>
 *
 */
package kinetic.client.advanced;
