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
package com.seagate.kinetic.simulator.internal.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;

/**
 * The command manager serves as a dispatcher to route each request to its
 * associated handler to process the request command.
 * 
 * @see CommandHandler
 * 
 * @author chiaming
 *
 */
public class CommandManager {

    private final static Logger logger = Logger.getLogger(CommandManager.class
            .getName());

    // simulator engine.
    private SimulatorEngine engine = null;

    // handler map.
    Map<MessageType, CommandHandler> handlerMap = new HashMap<MessageType, CommandHandler>();

    /**
     * Constructor to create this instance.
     * 
     * @param engine
     *            the current simulator engine.
     */
    public CommandManager(SimulatorEngine engine) {
        this.engine = engine;

        // initialize the handlers
        this.init();
    }

    /**
     * Initialize the service handlers.
     */
    private void init() {

        handlerMap.put(MessageType.NOOP, new NoOpHandler());
        handlerMap.put(MessageType.FLUSHALLDATA, new FlushOpHandler());
        handlerMap.put(MessageType.PINOP, new PinOpHandler());

        CommandHandler kvHandler = new KeyValueOpHandler();
        handlerMap.put(MessageType.PUT, kvHandler);
        handlerMap.put(MessageType.GET, kvHandler);
        handlerMap.put(MessageType.DELETE, kvHandler);
        handlerMap.put(MessageType.GETNEXT, kvHandler);
        handlerMap.put(MessageType.GETPREVIOUS, kvHandler);

        handlerMap.put(MessageType.GETKEYRANGE, new RangeOpHandler());

        this.handlerMap.put(MessageType.SECURITY, new SecurityOpHandler());

        this.handlerMap.put(MessageType.SETUP, new SetupOpHandler());

        this.handlerMap.put(MessageType.GETLOG, new GetLogOpHandler());

        this.handlerMap.put(MessageType.PEER2PEERPUSH, new P2POpHandler());

        this.handlerMap.put(MessageType.MEDIASCAN, new MediaScanOpHandler());

        this.handlerMap.put(MessageType.MEDIAOPTIMIZE,
                new MediaOptimizeOpHandler());

        for (CommandHandler handler : handlerMap.values()) {
            handler.init(engine);
        }
    }

    /**
     * Get the associated command handler based the specified message type.
     * 
     * @param mtype
     *            the request message type.
     * @return the command handler for the specified message type.
     */
    public CommandHandler getHandler(MessageType mtype) {

        // get handler instance from map.
        CommandHandler handler = this.handlerMap.get(mtype);

        // use a no-op handler if no handler is associated with the request.
        if (handler == null) {
            handler = this.handlerMap.get(MessageType.NOOP);
            logger.warning("No handler found for the requested message type: "
                    + mtype + ", no op is performed.");
        }

        return handler;
    }

    /**
     * Close all handlers associated with this simulator.
     */
    public void close() {
        for (CommandHandler handler : handlerMap.values()) {
            handler.close();
        }
    }
}
