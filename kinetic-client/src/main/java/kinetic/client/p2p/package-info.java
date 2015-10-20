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
 * Kinetic Peer to Peer Operations API. 
 * <p>
 * A Kinetic application uses the peer to peer API provided in this package to interface with
 * Kinetic services for peer to peer operations.</P>
 * <p>
 * The boot-strap API for using the peer to peer client library is
 * <code>KineticP2PClientFactory.createInstance(ClientConfiguration)</code>.</P>
 * <p>
 * Kinetic applications construct a new instance of <code>ClientConfiguration</code>
 * and set the appropriate configurations (such as server host/port) to the
 * configuration instance. Applications then invoke the static createInstance
 * method <code>KineticP2PClientFactory.createInstance(ClientConfiguration)</code> to obtain a new
 * instance of KineticP2pClient.</p>
 *
 */
package kinetic.client.p2p;
