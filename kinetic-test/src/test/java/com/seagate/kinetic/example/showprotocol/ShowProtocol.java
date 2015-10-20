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
package com.seagate.kinetic.example.showprotocol;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

/**
 * Example to print/log Kinetic protocol messages.
 * 
 * @author chiaming
 *
 */
public class ShowProtocol {

    public static void main(String[] args) throws KineticException {
        
        KineticClient cclient = null;
        
        //define system property to log inbound protocol messages.
        System.setProperty("kinetic.io.in", "true");
        
       //define system property to log outbound protocol messages.
        System.setProperty("kinetic.io.out", "true");

        ClientConfiguration config = new ClientConfiguration();

        cclient = KineticClientFactory.createInstance(config);

        Entry eentry = new Entry();

        byte[] key = "hello".getBytes();

        eentry.setKey(key);
        byte[] value = "world".getBytes();
        eentry.setValue(value);

        cclient.putForced(eentry);

        Entry entry2 = cclient.get(key);

        cclient.close();

        System.out.println("**** client closed., entry=" + entry2.getValue().length);

    }

}
