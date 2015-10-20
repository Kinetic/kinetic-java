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
