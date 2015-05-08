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
 * This exception indicates a batch commit (END_BATCH) was unsuccessful. All
 * commands performed within the batch were not committed to the persistent
 * store.
 * 
 * @see BatchOperation
 * @author chiaming
 *
 */
public class BatchAbortedException extends KineticException {

    private static final long serialVersionUID = 8738331797271144047L;

    private int index = -1;

    /**
     * {@inheritDoc}
     */
    public BatchAbortedException() {
        ;
    }
    
    /**
     * {@inheritDoc}
     */
    public BatchAbortedException(String message) {
        super(message);
    }
    
    /**
     * {@inheritDoc}
     */
    public BatchAbortedException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public BatchAbortedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Get the failed command index number starting with 0 for the first
     * command. For example, if the second command failed, it returns 1;
     * 
     * @return the failed command index number starting with 0 for the first
     *         command
     */
    public int getFailedOperationIndex() {
        return index;
    }

    /**
     * Set failed operation command index within the batch.
     * 
     * @param index
     *            failed operation command index within the batch
     */
    public void setFailedOperationIndex(int index) {
        this.index = index;
    }

}
