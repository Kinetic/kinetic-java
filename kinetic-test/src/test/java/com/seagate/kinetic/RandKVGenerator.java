package com.seagate.kinetic;

public interface RandKVGenerator {
    // generate a key, if keySize == 0, a random size key will be generated
	String nextKey(int keySize);
	
	// get key from value, if valueSize == 0, a random size value will be generated
	String getValue(String key, int valueSize);
}
