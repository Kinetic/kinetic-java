package com.seagate.kinetic.simulator.persist;

import java.io.Closeable;

import com.google.protobuf.ByteString;

public interface BatchOperation<K, V> extends Closeable {

    public void put(ByteString key, KVValue value);

    public void delete(ByteString key);

    public void commit();

    public boolean isClosed();
}
