package com.seagate.kinetic.simulator.io.provider.nio;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;

public class BatchQueue {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(BatchQueue.class
            .getName());

    private ArrayList<KineticMessage> mlist = new ArrayList<KineticMessage>();

    private long cid = -1;

    private int queueDepth = 100;

    public static boolean isStartBatchMessage(KineticMessage request) {
        return (request.getCommand().getHeader().getMessageType() == MessageType.START_BATCH);
    }

    public BatchQueue(KineticMessage request) {
        // this batch op handler belongs to this connection
        this.cid = request.getCommand().getHeader().getConnectionID();
    }

    public void add(KineticMessage request) {
        // this.checkPermission(request);
        this.mlist.add(request);
    }

    /**
     * only one batch at a time is supported.
     * 
     * @param request
     */
    @SuppressWarnings("unused")
    private void checkPermission(KineticMessage request) {

        // check if request is from the same client/connection
        if (request.getCommand().getHeader().getConnectionID() != this.cid) {
            throw new RuntimeException("DB is locked by: " + cid
                    + ", request cid: "
                    + request.getCommand().getHeader().getConnectionID());
        }

        // check message type, only supports put/delete
        this.checkMessageType(request);

        // check if reached limit
        if (mlist.size() > this.queueDepth) {
            throw new RuntimeException("exceed max queue depth: "
                    + this.queueDepth);
        }
    }

    private void checkMessageType(KineticMessage request) {

        MessageType mtype = request.getCommand().getHeader().getMessageType();

        switch (mtype) {
        case PUT:
        case DELETE:
            return;
        default:
            throw new RuntimeException("invalid message type: " + mtype);
        }

    }

    public List<KineticMessage> getMessageList() {
        return this.mlist;
    }

    public boolean isSameClient(KineticMessage request) {
        return (cid == request.getCommand().getHeader().getConnectionID());
    }

}
