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
package kinetic.client;

/**
 * 
 * This exception is thrown when a version mismatch occurred during a PUT operation.
 * 
 * @see KineticClient#put(Entry, byte[])
 * 
 * @author James Hughes
 * @author chiaming yang
 *
 */
public class VersionMismatchException extends KineticException {

    private static final long serialVersionUID = -6591860012767352506L;

    public VersionMismatchException() {
        ;
    }

    public VersionMismatchException(String message) {
        super(message);
    }

    public VersionMismatchException(Throwable cause) {
        super(cause);
    }

    public VersionMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

}
