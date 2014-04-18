// Do NOT modify or remove this copyright and confidentiality notice!
//
// Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
//
// The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
// Portions are also trade secret. Any use, duplication, derivation, distribution
// or disclosure of this code, for any reason, not expressly authorized is
// prohibited. All other rights are expressly reserved by Seagate Technology, LLC.

package com.seagate.kinetic.simulator.persist;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.seagate.kinetic.proto.Kinetic.Message.Algorithm;

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
