/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.heartbeat.message;

public class ByteCounter {

    // access counter
    private long GET = 0; // get operation

    private long PUT = 0; // put operation

    private long DELETE = 0;

    private long GETNEXT = 0;

    private long GETPREVIOUS = 0;

    private long GETKEYRANGE = 0;

    private long GETVERSION = 0;

    private long SETUP = 0;

    private long GETLOG = 0;

    private long SECURITY = 0;

    // peer to peer push operation
    private long PEER2PEERPUSH = 0;

    public ByteCounter() {
        // TODO Auto-generated constructor stub
    }

    public void addGetCounter(int count) {
        this.GET += count;
    }

    public long getGetCounter() {
        return this.GET;
    }

    public void addPutCounter(int count) {
        this.PUT += count;
    }

    public long getPutCounter() {
        return this.PUT;
    }

    public void addDeleteCounter(int count) {
        this.DELETE += count;
    }

    public long getDeleteCounter() {
        return this.DELETE;
    }

    public void addGetNextCounter(int count) {
        this.GETNEXT += count;
    }

    public long getGetNextCounter() {
        return this.GETNEXT;
    }

    public void addGetPreviousCounter(int count) {
        this.GETPREVIOUS += count;
    }

    public long getGetPreviousCounter() {
        return this.GETPREVIOUS;
    }

    public void addGetKeyRangeCounter(int count) {
        this.GETKEYRANGE += count;
    }

    public long getGetKeyRangeCounter() {
        return this.GETKEYRANGE;
    }

    public void addGetVersionCounter(int count) {
        this.GETVERSION += count;
    }

    public long getGetVersionCounter() {
        return this.GETVERSION;
    }

    public synchronized void addGetLogCounter(int count) {
        this.GETLOG += count;
    }

    public long getGetLogCounter() {
        return this.GETLOG;
    }

    public synchronized void addSetupCounter(int count) {
        this.SETUP += count;
    }

    public long getSetupCounter() {
        return this.SETUP;
    }

    public synchronized void addSecurityCounter(int count) {
        this.SECURITY += count;
    }

    public long getSecurityCounter() {
        return this.SECURITY;
    }

    public synchronized void addP2PCounter(int count) {
        this.PEER2PEERPUSH += count;
    }

    public long getP2PCounter() {
        return this.PEER2PEERPUSH;
    }

}
