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
