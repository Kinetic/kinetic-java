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
package kinetic.client;

/**
 * 
 * This exception is thrown when a cluster version mismatch occurred during a Kinetic command operation.
 * 
 * @see KineticClient
 * 
 * @author James Hughes
 * @author chiaming yang
 *
 */
public class ClusterVersionFailureException extends KineticException {
    
    private static final long serialVersionUID = 429184580355264965L;

    public ClusterVersionFailureException() {
        ;
    }

    public ClusterVersionFailureException(String message) {
        super(message);
    }

    public ClusterVersionFailureException(Throwable cause) {
        super(cause);
    }

    public ClusterVersionFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
