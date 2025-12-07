package org.shavin.util;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A lightweight, thread-safe cache with time-based expiration.
 * Ideal for deduplicating gossip messages (Seen Cache).
 * * <p>It does NOT use a background thread. You must call {@link #prune()}
 * periodically (e.g., from your protocol loop) to reclaim memory.</p>
 *
 * @param <K> The type of key to store (e.g., Long for hashes)
 */
public class SimpleTimeCache<K> {

    // Stores Key -> Expiration Timestamp (in milliseconds)
    private final ConcurrentHashMap<K, Long> store = new ConcurrentHashMap<>();
    private final long ttlMillis;

    /**
     * @param ttlMillis How long an entry stays valid.
     */
    public SimpleTimeCache(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    /**
     * Marks a key as "seen" or "active".
     * Refreshes the TTL if it already exists.
     */
    public void put(K key) {
        store.put(key, System.currentTimeMillis() + ttlMillis);
    }

    /**
     * Checks if the key exists and has not expired.
     * Note: This does NOT automatically remove expired keys (lazy removal),
     * to keep this method fast and lock-free.
     */
    public boolean contains(K key) {
        Long expiration = store.get(key);
        if (expiration == null) {
            return false;
        }

        // If current time is past expiration, logically it's gone.
        // We can optionally remove it here, but strict read-only is faster.
        if (System.currentTimeMillis() > expiration) {
            return false;
        }

        return true;
    }

    /**
     * Iterates through the map and removes expired entries.
     * Call this periodically (e.g., every 1-5 seconds) from your main scheduler.
     * * @return number of items removed
     */
    public int prune() {
        long now = System.currentTimeMillis();
        int removedCount = 0;

        // ConcurrentHashMap iterator is safe to use while others modify the map
        Iterator<Map.Entry<K, Long>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, Long> entry = it.next();
            if (now > entry.getValue()) {
                it.remove();
                removedCount++;
            }
        }
        return removedCount;
    }

    public int size() {
        return store.size();
    }
}
