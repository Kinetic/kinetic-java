package com.seagate.kinetic.common.lib;

import com.google.protobuf.ByteString;

public interface KineticTagCalc {

    public ByteString calculateTag(byte[] value);

    public String getAlgoName();

}
