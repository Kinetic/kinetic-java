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
