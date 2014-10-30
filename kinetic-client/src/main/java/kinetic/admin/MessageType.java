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
package kinetic.admin;

public enum MessageType {
    INVALID_MESSAGE_TYPE, GET, GET_RESPONSE, PUT, PUT_RESPONSE, DELETE, DELETE_RESPONSE, GETNEXT, GETNEXT_RESPONSE, GETPREVIOUS, GETPREVIOUS_RESPONSE, GETKEYRANGE, GETKEYRANGE_RESPONSE, GETVERSION, GETVERSION_RESPONSE, SETUP, SETUP_RESPONSE, GETLOG, GETLOG_RESPONSE, SECURITY, SECURITY_RESPONSE, PEER2PEERPUSH, PEER2PEERPUSH_RESPONSE, NOOP, NOOP_RESPONSE, FLUSHALLDATA, FLUSHALLDATA_RESPONSE, PINOP, PINOP_RESPONSE, MEDIASCAN, MEDIASCAN_RESPONSE, MEDIAOPTIMIZE, MEDIAOPTIMIZE_RESPONSE;
}
