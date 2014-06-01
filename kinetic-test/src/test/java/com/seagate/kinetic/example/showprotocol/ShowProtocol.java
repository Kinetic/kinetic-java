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