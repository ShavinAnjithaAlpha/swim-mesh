package org.shavin.impl;

import org.shavin.api.member.MemberNode;
import org.shavin.member.MembershipEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Store for keep the membership events in a buffer.
 * Supported operations including enqueue events according to merge rules and dequeue events according to dissemination rules.
 * @author shavin
 */
public class MembershipEventStore {
    private final static Logger log = LoggerFactory.getLogger(MembershipEventStore.class);

    private final int maxEventsToKeep = 1000;
    private final int MIN_EFFECTIVE_CLUSTER_SIZE = 10;
    private final int SAFE_MTU = 1400;

    private final Map<Integer, MembershipEvent> membershipEventMap = new ConcurrentHashMap<>();
    private final List<MemberNode> members;


    private static MembershipEventStore instance;

    // Singleton pattern to ensure only one instance of MembershipEventStore exists
    public static MembershipEventStore getInstance(List<MemberNode> members, ThreadFactory threadFactory) {
        if (instance == null) {
            instance = new MembershipEventStore(members, threadFactory);
        }
        return instance;
    }

    private MembershipEventStore(List<MemberNode> members, ThreadFactory threadFactory) {
        // a private constructor to prevent instantiation outside of this class
        this.members = members;
    }

    private int calculateThreshold() {
        int N = members.size();

        int effectiveSize = Math.max(MIN_EFFECTIVE_CLUSTER_SIZE, N);

        // calculate the threshold value according to K-Random Dissemination Rule
        int thresholdValue = (int) Math.ceil(3 * Math.log(effectiveSize + 1));
        return thresholdValue;
    }

    public void enqueueEvent(MembershipEvent.Type type, MemberNode node) {
        // create a membership event
        MembershipEvent membershipEvent = new MembershipEvent(type, node);
        // call the enqueue method to add the event to the buffer
        enqueueEvent(membershipEvent);
    }

    public void enqueueEvent(MembershipEvent membershipEvent) {
        // merge the events with the same node id together, according to the merge logic below
        membershipEventMap.merge(membershipEvent.nodeId(), membershipEvent, (oldEvent, event) -> {
            // keep the event with the highest incarnation number
            if (oldEvent.incarnationNumber() > event.incarnationNumber()) {
                return oldEvent;
            } else {
                return event;
            }
        });
    }

    public List<MembershipEvent> getRecentEventsAndIncrement() {
        List<MembershipEvent> eventBatch = new ArrayList<>();
        int estimatedBytes = 0;

        Iterator<MembershipEvent> eventIterator = membershipEventMap.values().iterator();

        while (eventIterator.hasNext()) {
            // get the next event to be attached
            MembershipEvent membershipEvent = eventIterator.next();

            eventBatch.add(membershipEvent);

            // increase the dissemination number of the event
            membershipEvent.incrementDisseminationCount();

            estimatedBytes += 12;

            // remove the event from the buffer if its send enough times
            if (membershipEvent.disseminationCount() >= calculateThreshold()) {
                eventIterator.remove();
            }

            if (estimatedBytes > (SAFE_MTU - 42)) {
                break;
            }
        }

        return eventBatch;

    }
}
