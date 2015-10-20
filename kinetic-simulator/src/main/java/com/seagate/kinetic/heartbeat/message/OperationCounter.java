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
package com.seagate.kinetic.heartbeat.message;

/**
 * Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation,
 * distribution or disclosure of this code, for any reason, not expressly
 * authorized is prohibited. All other rights are expressly reserved by Seagate
 * Technology, LLC.
 */
public class OperationCounter {

    // access counter
    private int GET = 0; // get operation

    private int PUT = 0; // put operation

    private int DELETE = 0;

    private int GETNEXT = 0;

    private int GETPREVIOUS = 0;

    private int GETKEYRANGE = 0;

    private int GETVERSION = 0;

    // removed the following two
    // private final int STEALER = 0;
    // private final int DONOR = 0;

    private int SETUP = 0;

    private int GETLOG = 0;

    private int SECURITY = 0;

    // peer to peer push operation
    private int PEER2PEERPUSH = 0;

    public OperationCounter() {
        // TODO Auto-generated constructor stub
    }

    public synchronized void addGetCounter() {
        this.GET++;
    }

    public int getGetCounter() {
        return this.GET;
    }

    public synchronized void addPutCounter() {
        this.PUT++;
    }

    public int getPutCounter() {
        return this.PUT;
    }

    public synchronized void addDeleteCounter() {
        this.DELETE++;
    }

    public int getDeleteCounter() {
        return this.DELETE;
    }

    public synchronized void addGetNextCounter() {
        this.GETNEXT++;
    }

    public int getGetNextCounter() {
        return this.GETNEXT;
    }

    public synchronized void addGetPreviousCounter() {
        this.GETPREVIOUS++;
    }

    public int getGetPreviousCounter() {
        return this.GETPREVIOUS;
    }

    public synchronized void addGetKeyRangeCounter() {
        this.GETKEYRANGE++;
    }

    public int getGetKeyRangeCounter() {
        return this.GETKEYRANGE;
    }

    public synchronized void addGetVersionCounter() {
        this.GETVERSION++;
    }

    public int getGetVersionCounter() {
        return this.GETVERSION;
    }

    public synchronized void addGetLogCounter() {
        this.GETLOG++;
    }

    public int getGetLogCounter() {
        return this.GETLOG;
    }

    public synchronized void addSetupCounter() {
        this.SETUP++;
    }

    public int getSetupCounter() {
        return this.SETUP;
    }

    public synchronized void addSecurityCounter() {
        this.SECURITY++;
    }

    public int getSecurityCounter() {
        return this.SECURITY;
    }

    public synchronized void addP2PCounter() {
        this.PEER2PEERPUSH++;
    }

    public int getP2PCounter() {
        return this.PEER2PEERPUSH;
    }
}
