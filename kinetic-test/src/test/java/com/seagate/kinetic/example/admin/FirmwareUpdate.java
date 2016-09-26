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
package com.seagate.kinetic.example.admin;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import kinetic.admin.AdminClientConfiguration;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.client.KineticException;

/**
 * 
 * Kinetic Firmware update example.
 * <p>
 *  
 * @author chiaming
 *
 */
public class FirmwareUpdate {
    
    /**
     * Perform firmware update based on the specified admin client 
     * configuration and firmware bytes.
     * @param cc admin client configuration.
     * @param data firmware bytes
     * @throws KineticException if any internal error occur.
     */
    @SuppressWarnings("deprecation")
    public static void updateFirmware(AdminClientConfiguration cc, byte[] data) throws KineticException {
        System.out.println ("updating firmware, size=" + data.length);
   
        // get admin client instance
        KineticAdminClient adminClient = KineticAdminClientFactory
                .createInstance(cc);
       
        try {
            // perform update
            adminClient.firmwareDownload(null, data);
            System.out.println ("updating firmware successfully , size=" + data.length);
        } catch (KineticException ke) {
            ke.printStackTrace();
        } finally {
            // close client
            adminClient.close();
        }
        
    }
    
    /**
     * Read kinetic firmware update file from the specified path.
     * 
     * @param path location of the kinetic firmware update file
     * @return a byte array that contains the firmware bytes
     * @throws IOException if any internal error occurred
     */
    public static byte[] loadFirmware (String path) throws IOException {
        
        // get file input stream from the specified path
        FileInputStream fis = new FileInputStream(path);
        
        // read chunk size
        int readSize = 1024 * 1024;
        
        // done flag
        boolean done = false;
        
        // byte array output stream (output container) to hold data read from file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // byte array to hold each read chunk
        byte[] bytes = new byte[readSize];

        try {
            while (!done) {
                
                // read chunk from file
                int num = fis.read(bytes);
                
                if (num > 0) {
                    // put to container
                    baos.write(bytes, 0, num);
                } else {
                    if (num < 0) {
                        // end of file
                        done = true;
                    }
                }

            }
        } finally {
            // close read stream
            fis.close();
        }
        
        // flush buffer
        baos.flush();
        
        // get the bytes in the container
        byte[] firmware = baos.toByteArray();
        
        // return firmware bytes
        return firmware;
    }

    /**
     * Program to read Kinetic formware file from the specified path and drive IP then perform 
     * firmware update on the specified drive IP.
     * 
     * @param args firmware file path and drive IP.
     * @throws IOException if any IO error occurred during operation.
     * @throws KineticException if any kinetic service error occurred.
     */
    public static void main(String[] args) throws IOException, KineticException {
     
        /**
         * check if required parameter is specified. 
         */
        if (args.length != 2) {
            throw new RuntimeException(
                    "Uasge java FiramdownloadExample firmwarePath driveIp");
        }

        // firmware file path
        String path = args[0];

        // drive IP
        String host = args[1];

        // read firmware bytes
        byte[] firmware = loadFirmware(path);
        
        // construct admin client configuration
        AdminClientConfiguration acc = new AdminClientConfiguration();
        
        // use none-ssl transport
        acc.setUseSsl(false);
        
        // set drive IP
        acc.setHost(host);
        // set TCP (none-ssl) service port
        acc.setPort(8123);

        // perform firmware update
        updateFirmware(acc, firmware);
    }

}

