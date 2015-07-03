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
package com.seagate.kinetic.common.lib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;

/**
 * Kinetic tag calculation with Message digest algorithm utils.
 * 
 * @author chiaming
 *
 */
public class MessageDigestTagCalc implements KineticTagCalc {

    public static final String SHA1 = "SHA-1";

    public static final String SHA2 = "SHA-256";

    private final static Logger logger = Logger.getLogger(MessageDigestTagCalc.class
            .getName());

    private MessageDigest md = null;

    private String algoName = null;

    public MessageDigestTagCalc(String algoName) {

        this.algoName = algoName;
        this.init();
    }

    @Override
    public synchronized ByteString calculateTag(byte[] value) {

        // init to empty byte if null
        if (value == null) {
            value = new byte[0];
        }

        byte[] digest = null;
        ByteString tag = null;

        try {
            // calculate
            digest = md.digest(value);
            // to byte string
            tag = ByteString.copyFrom(digest);
        } finally {
            // reset for further use
            md.reset();
        }

        return tag;
    }

    private void init() {
        try {
            md = MessageDigest.getInstance(algoName);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    public String getAlgoName() {
        return this.algoName;
    }
}
