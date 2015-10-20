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
package com.seagate.kinetic.simulator.io.provider.nio;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.KineticMessage;

public class BatchQueue {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(BatchQueue.class
            .getName());

    private ArrayList<KineticMessage> mlist = new ArrayList<KineticMessage>();

    public BatchQueue(KineticMessage request) {
        ;
    }

    public void add(KineticMessage request) {
        // this.checkPermission(request);
        this.mlist.add(request);
    }

    public List<KineticMessage> getMessageList() {
        return this.mlist;
    }

    public int size() {
        return this.mlist.size();
    }

    public void clear() {
        this.mlist.clear();
    }

}
