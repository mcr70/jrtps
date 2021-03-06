package net.sf.jrtps.udds;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.Guid;

class BuiltinSubscriptionDataListener extends BuiltinListener implements SampleListener<SubscriptionData> {
   private static final Logger log = LoggerFactory.getLogger(BuiltinSubscriptionDataListener.class);

   private Map<Guid, SubscriptionData> discoveredReaders;

   BuiltinSubscriptionDataListener(Participant p, Map<Guid, SubscriptionData> discoveredReaders) {
      super(p);
      this.discoveredReaders = discoveredReaders;
   }

   /**
    * Handle discovered SubscriptionData. If SubscriptionData represents an
    * user defined Reader, and this participant has a Writer for same topic,
    * send writers history cache to reader.
    * 
    * @param samples
    */
   @Override
   public void onSamples(List<Sample<SubscriptionData>> samples) {
      for (Sample<SubscriptionData> sdSample : samples) {
         SubscriptionData sd = sdSample.getData();

         Guid key = sd.getBuiltinTopicKey();
         if (discoveredReaders.put(key, sd) == null) {
            log.info("Discovered a new subscription for topic {}: {}, type {}, QoS: {}", 
                  sd.getTopicName(), key, sd.getTypeName(), sd.getQualityOfService());
            fireReaderDetected(sd);
         }

         List<DataWriter<?>> writers = participant.getWritersForTopic(sd.getTopicName());
         log.debug("considering {} writers for topic {}, is disposed: {}", writers.size(), 
               sd.getTopicName(), sdSample.isDisposed());

         for (DataWriter<?> w : writers) {
            if (!sdSample.isDisposed()) {
               // Not disposed, check for compatible qos
               QualityOfService requested = sd.getQualityOfService();
               QualityOfService offered = w.getRTPSWriter().getQualityOfService();
               log.trace("Check for compatible QoS for {} and {}", w.getRTPSWriter().getGuid().getEntityId(), 
                     sd.getBuiltinTopicKey().getEntityId());

               if (offered.isCompatibleWith(requested)) {
                  w.addMatchedReader(sd);
               } 
               else {
                  // Reader might have been previously associated. Remove association.
                  w.removeMatchedReader(sd); 
                  log.warn("Discovered reader had incompatible QoS with writer: {}, local writers QoS: {}", sd, w
                        .getRTPSWriter().getQualityOfService());
                  w.inconsistentQoS(sd);
               }
            } 
            else {
               log.debug("SubscriptionData was disposed, removing matched writer");
               // Associated and sample is dispose -> remove association
               w.removeMatchedReader(sd);
            }
         }

         if (sdSample.isDisposed()) {
            discoveredReaders.remove(key);
         }
      }
   }
}
