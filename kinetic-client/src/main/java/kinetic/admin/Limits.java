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
 * 
 * KineticAaminClient getLog Limits container.
 * <p>
 * A Limits contains maxKeySize, maxValueSize, maxVersionSize, maxTagSize,
 * maxConnections, maxOutstandingReadRequests, maxOutstandingWriteRequests,
 * maxMessageSize.
 * <p>
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
     * Set the max key size field content of the limits.
     * 
     * @param key
     *            the content to be set to the max key size field.
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
     * Set the max value size field content of the limits.
     * 
     * @param key
     *            the content to be set to the max value size field.
     */
    public void setMaxValueSize(int maxValueSize) {
        this.maxValueSize = maxValueSize;
    }

    /**
     * Get the value of max version size.
     * 
     * @return the content of the max version size field.
     */
    public int getMaxVersionSize() {
        return maxVersionSize;
    }

    /**
     * Set the max version size field content of the limits.
     * 
     * @param key
     *            the content to be set to the max version size field.
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
     * Set the max tag size field content of the limits.
     * 
     * @param key
     *            the content to be set to the max tag field.
     */
    public void setMaxTagSize(int maxTagSize) {
        this.maxTagSize = maxTagSize;
    }

    /**
     * Get the value of max connections.
     * 
     * @return the content of the max connections field.
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Set the max connections field content of the limits.
     * 
     * @param key
     *            the content to be set to the max connections field.
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
     * Set the max out standing read requests field content of the limits.
     * 
     * @param key
     *            the content to be set to the max out standing read requests
     *            field.
     */
    public void setMaxOutstandingReadRequests(int maxOutstandingReadRequests) {
        this.maxOutstandingReadRequests = maxOutstandingReadRequests;
    }

    /**
     * Get the value of max out standing write requests.
     * 
     * @return the content of the max out standing write requests field.
     */
    public int getMaxOutstandingWriteRequests() {
        return maxOutstandingWriteRequests;
    }

    /**
     * Set the max out standing write requests field content of the limits.
     * 
     * @param key
     *            the content to be set to the max out standing write requests
     *            field.
     */
    public void setMaxOutstandingWriteRequests(int maxOutstandingWriteRequests) {
        this.maxOutstandingWriteRequests = maxOutstandingWriteRequests;
    }

    /**
     * Get the value of max message size.
     * 
     * @return the content of the max message size.
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * Set the max message size field content of the limits.
     * 
     * @param key
     *            the content to be set to the max message size field.
     */
    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }
    
    /**
     * Get the value of max key range size.
     * 
     * @return the content of the max key range size.
     */
    public int getMaxKeyRangeCount() {
        return maxKeyRangeCount;
    }

    /**
     * Set the max key range size field content of the limits.
     * 
     * @param key
     *            the content to be set to the max key range size field.
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
