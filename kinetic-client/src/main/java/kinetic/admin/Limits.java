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
package kinetic.admin;

/**
 * 
 * KineticAaminClient getLog Limits container.
 * <p>
 * A Limits contains maxKeySize, maxValueSize, maxVersionSize, maxTagSize,
 * maxConnections, maxOutstandingReadRequests, maxOutstandingWriteRequests,
 * maxMessageSize.
 * </p>
 * 
 * @see KineticLog
 * @see KineticLogType
 */
public class Limits {
    // max key size
    private int maxKeySize = 0;

    // max value size
    private int maxValueSize = 0;

    // max version size
    private int maxVersionSize = 0;

    // max tag size
    private int maxTagSize = 0;

    // max connection
    private int maxConnections = 0;

    // max out standing read request
    private int maxOutstandingReadRequests = 0;

    // max out standing write request
    private int maxOutstandingWriteRequests = 0;

    // max message size
    private int maxMessageSize = 0;
    
    // max key range count;
    private int maxKeyRangeCount = 0;
    
    //max identity count
    private int maxIdentityCount = -1;

    /**
     * Get the value of max key size.
     * 
     * @return the content of the max key size field.
     */
    public int getMaxKeySize() {
        return maxKeySize;
    }

    /**
     * Set the max allowed key size.
     * 
     * @param maxKeySize
     *            the max allowed key size.
     */
    public void setMaxKeySize(int maxKeySize) {
        this.maxKeySize = maxKeySize;
    }

    /**
     * Get the value of max value size.
     * 
     * @return the content of the max value size field.
     */
    public int getMaxValueSize() {
        return maxValueSize;
    }

    /**
     * Set the max allowed value size.
     * 
     * @param maxValueSize
     *            the max allowed value size.
     */
    public void setMaxValueSize(int maxValueSize) {
        this.maxValueSize = maxValueSize;
    }

    /**
     * Get max allowed version size.
     * 
     * @return max allowed version size.
     */
    public int getMaxVersionSize() {
        return maxVersionSize;
    }

    /**
     * Set the max allowed version size.
     * 
     * @param maxVersionSize
     *            the max allowed version size.
     */
    public void setMaxVersionSize(int maxVersionSize) {
        this.maxVersionSize = maxVersionSize;
    }

    /**
     * Get the value of max tag size.
     * 
     * @return the content of the max tag size field.
     */
    public int getMaxTagSize() {
        return maxTagSize;
    }

    /**
     * Set the max tag size.
     * 
     * @param maxTagSize
     *            the max tag size
     */
    public void setMaxTagSize(int maxTagSize) {
        this.maxTagSize = maxTagSize;
    }

    /**
     * Get max allowed concurrent connections.
     * 
     * @return max allowed concurrent connections
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Set max allowed concurrent connections
     * 
     * @param maxConnections
     *            the max allowed concurrent connections
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * Get the value of max out standing read requests.
     * 
     * @return the content of the max out standing read requests field.
     */
    public int getMaxOutstandingReadRequests() {
        return maxOutstandingReadRequests;
    }

    /**
     * Set the max allowed out standing read requests.
     * 
     * @param maxOutstandingReadRequests
     *            the max allowed out standing read requests
     */
    public void setMaxOutstandingReadRequests(int maxOutstandingReadRequests) {
        this.maxOutstandingReadRequests = maxOutstandingReadRequests;
    }

    /**
     * Get max allowed out-standing write requests.
     * 
     * @return tmax allowed out-standing write requests.
     */
    public int getMaxOutstandingWriteRequests() {
        return maxOutstandingWriteRequests;
    }

    /**
     * Set max allowed out-standing write requests
     * 
     * @param maxOutstandingWriteRequests
     *            max allowed out-standing write requests
     */
    public void setMaxOutstandingWriteRequests(int maxOutstandingWriteRequests) {
        this.maxOutstandingWriteRequests = maxOutstandingWriteRequests;
    }

    /**
     * Get max allowed message size.
     * 
     * @return max allowed message size
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * Set the max allowed message size.
     * 
     * @param maxMessageSize
     *            tthe max allowed message size
     */
    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }
    
    /**
     * Get max allowed number of keys can be returned from getKeyRange.
     * 
     * @return max allowed number of keys can be returned from getKeyRange.
     */
    public int getMaxKeyRangeCount() {
        return maxKeyRangeCount;
    }

    /**
     * Set max allowed number of keys can be returned from getKeyRange.
     * 
     * @param maxKeyRangeCount
     *            max allowed number of keys can be returned from getKeyRange.
     */
    public void setMaxKeyRangeCount(int maxKeyRangeCount) {
        this.maxKeyRangeCount = maxKeyRangeCount;
    }
    
    /**
     * Get max identity count. -1 means not enforced.
     * 
     * @return max identity count.
     */
    public int getMaxIdentityCount() {
        return this.maxIdentityCount;
    }
    
    /**
     * Set max identity count.
     * 
     * @param maxIdentityCount the max identity count to be set.
     */
    public void setMaxIdentityCount (int maxIdentityCount) {
        this.maxIdentityCount = maxIdentityCount;
    }
    
    
}
