package org.shavin.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.shavin.member.MemberNode;
import org.shavin.member.MembershipEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store for keep the membership events in a buffer.
 * Supported operations including enqueue events according to merge rules and dequeue events according to dissemination rules.
 * @author shavin
 */
public class MembershipEventStore {
    private final static Logger logger = LogManager.getLogger(MembershipEventStore.class);

    private final int maxEventsToKeep = 1000;
    private final int THRESHOLD = 10; // TODO: should be calculated according to cluster capacity
    private final int SAFE_MTU = 1400;

    private final Map<Integer, MembershipEvent> membershipEventMap = new ConcurrentHashMap<>();


    private static MembershipEventStore instance;

    // Singleton pattern to ensure only one instance of MembershipEventStore exists
    public static MembershipEventStore getInstance() {
        if (instance == null) {
            instance = new MembershipEventStore();
        }
        return instance;
    }

    private MembershipEventStore() {
        // private constructor to prevent instantiation outside of this class
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
            if (membershipEvent.disseminationCount() >= THRESHOLD) {
                eventIterator.remove();
            }

            if (estimatedBytes > (SAFE_MTU - 42)) {
                break;
            }
        }

        return eventBatch;

    }
}
