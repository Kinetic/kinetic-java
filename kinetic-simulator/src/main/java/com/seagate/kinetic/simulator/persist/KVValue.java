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

package com.seagate.kinetic.simulator.persist;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.seagate.kinetic.proto.Kinetic.Command.Algorithm;

public class KVValue {

    public com.seagate.kinetic.proto.KineticDb.Versioned.Builder m = com.seagate.kinetic.proto.KineticDb.Versioned
            .newBuilder();

    // brings in the serialized information from Protocol Buffers
    public KVValue(byte[] value) {
        try {
            m.mergeFrom(value);
        } catch (InvalidProtocolBufferException e) {

            e.printStackTrace();
        }
    }

    // brings in the serialized information from Protocol Buffers
    public KVValue(com.seagate.kinetic.proto.KineticDb.Versioned value) {
        m.mergeFrom(value);
    }

    // TODO add algorithm
    public KVValue(ByteString key, ByteString version, ByteString tag,
            Algorithm algo,
            ByteString data) {

        if (key != null) {
            setKeyOf(key);
        }

        if (version != null) {
            setVersion(version);
        }

        if (tag != null) {
            setTag(tag);
        }

        if (algo != null) {
            this.setAlgorithm(algo);
        }

        if (data != null) {
            setData(data);
        }
    }

    public KVValue(ByteString v) {
        this(null, null, null, null, v);
    }

    public void setKeyOf(ByteString keyOf) {
        // m.setKeyOf(keyOf);
        m.getMetadataBuilder().setKey(keyOf);
    }

    public boolean hasKeyOf() {
        // return m.hasKeyOf();
        return m.getMetadataBuilder().hasKey();
    }

    public ByteString getKeyOf() {
        // return m.getKeyOf();
        return m.getMetadataBuilder().getKey();
    }

    public void setVersion(ByteString version) {
        // m.setVersion(version);

        m.getMetadataBuilder().setDbVersion(version);
    }

    public boolean hasVersion() {
        // return m.hasVersion();
        return m.getMetadataBuilder().hasDbVersion();
    }

    public ByteString getVersion() {
        // return m.getVersion();
        return m.getMetadataBuilder().getDbVersion();
    }

    public void setTag(ByteString Tag) {
        // m.setTag(Tag);
        m.getMetadataBuilder().setTag(Tag);
    }

    public boolean hasTag() {
        // return m.hasTag();
        return m.getMetadataBuilder().hasTag();
    }

    public ByteString getTag() {
        // return m.getTag();
        return m.getMetadataBuilder().getTag();
    }

    public void setAlgorithm(Algorithm algo) {
        if (algo != null) {
            m.getMetadataBuilder().setAlgorithm(algo);
        }
    }

    public Algorithm getAlgorithm() {
        // return m.getTag();
        return m.getMetadataBuilder().getAlgorithm();
    }

    public boolean hasAlgorithm() {
        return m.getMetadataBuilder().hasAlgorithm();
    }

    public void setData(ByteString Data) {
        // m.setData(Data);
        m.setValue(Data);
    }

    public boolean hasData() {
        // return m.hasData();

        return m.hasValue();
    }

    public ByteString getData() {
        // return m.getData();
        return m.getValue();
    }

    public byte[] toByteArray() {
        return m.build().toByteArray();
    }

}
