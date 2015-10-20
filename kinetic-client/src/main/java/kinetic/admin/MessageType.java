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
package kinetic.admin;

public enum MessageType {
    INVALID_MESSAGE_TYPE, GET, GET_RESPONSE, PUT, PUT_RESPONSE, DELETE, DELETE_RESPONSE, GETNEXT, GETNEXT_RESPONSE, GETPREVIOUS, GETPREVIOUS_RESPONSE, GETKEYRANGE, GETKEYRANGE_RESPONSE, GETVERSION, GETVERSION_RESPONSE, SETUP, SETUP_RESPONSE, GETLOG, GETLOG_RESPONSE, SECURITY, SECURITY_RESPONSE, PEER2PEERPUSH, PEER2PEERPUSH_RESPONSE, NOOP, NOOP_RESPONSE, FLUSHALLDATA, FLUSHALLDATA_RESPONSE, PINOP, PINOP_RESPONSE, MEDIASCAN, MEDIASCAN_RESPONSE, MEDIAOPTIMIZE, MEDIAOPTIMIZE_RESPONSE;
}
