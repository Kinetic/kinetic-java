/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.seagate.kinetic.simulator.internal;

/**
 * 
 * Base (super) class for all fault injected exceptions for the simulator.
 * 
 * @author chiaming
 * 
 */
public class FaultInjectedException extends RuntimeException {

	private static final long serialVersionUID = -5384109392179754675L;

	public FaultInjectedException() {
		;
	}

	public FaultInjectedException(String message) {
		super(message);

	}

	public FaultInjectedException(Throwable cause) {
		super(cause);

	}

	public FaultInjectedException(String message, Throwable cause) {
		super(message, cause);

	}

}
