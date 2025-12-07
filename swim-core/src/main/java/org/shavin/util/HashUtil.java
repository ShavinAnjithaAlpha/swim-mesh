package org.shavin.util;

public class HashUtil {

    // FNV-1a 64-bit constants
    private static final long FNV_64_INIT = 0xcbf29ce484222325L;
    private static final long FNV_64_PRIME = 0x100000001b3L;

    /**
     * Calculates a 64-bit hash using the FNV-1a algorithm.
     * Ideal for generating unique IDs for byte arrays/messages.
     */
    public static long hash64(byte[] data) {
        long hash = FNV_64_INIT;
        for (byte b : data) {
            // XOR with the byte (bitwise AND 0xff to treat as unsigned)
            hash ^= (b & 0xff);
            // Multiply by the prime
            hash *= FNV_64_PRIME;
        }
        return hash;
    }

    /**
     * Helper to get a Hex String representation of the hash (e.g., for logging).
     */
    public static String hashToHex(byte[] data) {
        return Long.toHexString(hash64(data));
    }
}
