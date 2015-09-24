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
