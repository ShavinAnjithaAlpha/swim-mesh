package org.shavin.swim.impl;

import org.shavin.swim.api.event.ClusterEventListener;
import org.shavin.swim.api.member.MemberNode;
import org.shavin.swim.messages.CustomUserData;
import org.shavin.swim.util.HashUtil;
import org.shavin.swim.util.SimpleTimeCache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * CustomDataManager handles the dissemination, retrieval, and processing of custom data
 * within a cluster system. It provides mechanisms for efficient data broadcasting,
 * reception, and storage while maintaining constraints such as maximum data size and
 * safe transmission thresholds.
 *
 * This class integrates a time-based cache to prevent redundant processing of previously
 * seen data and adheres to a dissemination threshold algorithm for controlling data
 * spread within the cluster. Listeners can register to receive notifications about
 * custom data reception.
 */
public class CustomDataManager {

    private final static int CACHE_PRUNE_INTERVAL_MS = 10000;
    private final static int MIN_EFFECTIVE_CLUSTER_SIZE = 10;
    private final static int MAX_SIZE_PER_MESSAGE_IN_BYTES = 256;
    private final static int SAFE_MTU = 1400;

    private final ConcurrentHashMap<Long, CustomUserData> outgoingData = new ConcurrentHashMap<>();
    private final SimpleTimeCache<Long> seenCache = new SimpleTimeCache<>(10000);
    private final List<ClusterEventListener> listeners;
    private final List<MemberNode> members;

    public CustomDataManager(List<ClusterEventListener> listeners, ScheduledExecutorService executorService, List<MemberNode> members) {
        this.listeners = listeners;
        this.members = members;
//        executorService.scheduleAtFixedRate(seenCache::prune, 0, CACHE_PRUNE_INTERVAL_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private int calculateThreshold() {
        int N = members.size();

        int effectiveSize = Math.max(MIN_EFFECTIVE_CLUSTER_SIZE, N);

        // calculate the threshold value according to K-Random Dissemination Rule
        int thresholdValue = (int) Math.ceil(3 * Math.log(effectiveSize + 1));
        return thresholdValue;
    }

    public void broadcastData(byte[] data) {
        if (data.length > MAX_SIZE_PER_MESSAGE_IN_BYTES) {
            throw new IllegalArgumentException("Data size exceeds max allowed size");
        }

        // otherwise add to the outgoing data map
        outgoingData.put(HashUtil.hash64(data), new CustomUserData(data));
    }

    public List<CustomUserData> getDataToSend() {
        List<CustomUserData> dataToBeSend = new ArrayList<>();
        int bytesToSend = 0;

        // get the value iterator of the outgoing data buffer
        Iterator<CustomUserData> iterator = outgoingData.values().iterator();

        while (iterator.hasNext())  {
            CustomUserData userData = iterator.next();

            if (bytesToSend + userData.getData().length + 4 >= SAFE_MTU - 42) {
                break;
            }

            dataToBeSend.add(userData);

            userData.incrementDissemationCount();

            if (userData.disseminationCount() >= calculateThreshold()) { // if the dissemination count is greater than or equal to THRESHOLD, remove the entry from the buffer
                iterator.remove();
            }

            seenCache.put(HashUtil.hash64(userData.getData()));
            bytesToSend += userData.getData().length + 4;
        }

        return dataToBeSend;
    }

    private void receiveUserData(byte[] data) {
        // calculate the hash
        long hash = HashUtil.hash64(data);
        if (!seenCache.contains(hash)) {
            // add to the seen cache
            seenCache.put(hash);
            // add to the outgoing data buffer
            outgoingData.put(hash, new CustomUserData(data));

            // notify the protocol layer of the new data
            listeners.forEach(listener -> {
                listener.onReceiveData(data);
            });
        }
    }

    public void onReceive(List<CustomUserData> customUserDataList) {
        customUserDataList.forEach(userData -> {
            receiveUserData(userData.getData());
        });
    }

}
