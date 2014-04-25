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

/**
 * Kinetic drive utilization information container.
 * 
 */
public class Utilization {
	// device name
	private String name;

	// device utility
	private float utility;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getUtility() {
		return utility;
	}

	public void setUtility(float utility) {
		this.utility = utility;
	}


}
