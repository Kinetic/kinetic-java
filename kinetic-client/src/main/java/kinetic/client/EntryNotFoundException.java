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
 * This exception is thrown to indicate that an entry is expected but unable to obtain from the service.  
 * 
 * @author chiaming
 * 
 * @see KineticException
 */
public class EntryNotFoundException extends KineticException {

    private static final long serialVersionUID = -2377497794808030692L;
    
    // the entry that wasn't found
    private Entry entry;

    public EntryNotFoundException() {
        ;
    }

    public EntryNotFoundException(String message) {
        super(message);
    }

    public EntryNotFoundException(Throwable cause) {
        super(cause);
    }

    public EntryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntryNotFoundException(Entry entry) {
        this.entry = entry;
    }

    public EntryNotFoundException(String message, Entry entry) {
        super(message);
        this.entry = entry;
    }

    public EntryNotFoundException(Throwable cause, Entry entry) {
        super(cause);
        this.entry = entry;
    }

    public EntryNotFoundException(String message, Throwable cause, Entry entry) {
        super(message, cause);
        this.entry = entry;
    }

}
