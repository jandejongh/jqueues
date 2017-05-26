package nl.jdj.jqueues.r5.entity.jq.job.selflistening;

import java.util.Map;
import nl.jdj.jqueues.r5.entity.jq.job.SimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for {@link DefaultSelfListeningSimJob}s.
 *
 * @param <Q>  The queue type for jobs.
 * 
 * @see DefaultSelfListeningSimJob
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public class DefaultSelfListeningSimJobFactory<Q extends SimQueue>
implements SimJobFactory<DefaultSelfListeningSimJob, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a new {@link DefaultSelfListeningSimJob} with given parameters.
   * 
   * @return A new {@link DefaultSelfListeningSimJob} with given parameters.
   * 
   * @see DefaultSelfListeningSimJob#DefaultSelfListeningSimJob
   * 
   */
  @Override
  public DefaultSelfListeningSimJob newInstance
  (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultSelfListeningSimJob (eventList, name, requestedServiceTimeMap);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
