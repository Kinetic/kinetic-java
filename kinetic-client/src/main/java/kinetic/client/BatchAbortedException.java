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

    public BatchAbortedException() {
        ;
    }
    
    public BatchAbortedException(String message) {
        super(message);
    }
    
    public BatchAbortedException(Throwable cause) {
        super(cause);
    }

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
