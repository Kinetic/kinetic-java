/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
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
