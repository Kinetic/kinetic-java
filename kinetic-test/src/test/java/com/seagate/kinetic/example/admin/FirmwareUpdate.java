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

