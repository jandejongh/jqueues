package nl.jdj.jqueues.r5.entity.jq.job;

import java.util.Map;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for {@link DefaultSimJob}s.
 *
 * @param <Q>  The queue type for jobs.
 * 
 * @see DefaultSimJob
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
public class DefaultSimJobFactory<Q extends SimQueue>
implements SimJobFactory<DefaultSimJob, Q>
{

  /** Returns a new {@link DefaultSimJob} with given parameters.
   * 
   * @return A new {@link DefaultSimJob} with given parameters.
   * 
   * @see DefaultSimJob#DefaultSimJob
   * 
   */
  @Override
  public DefaultSimJob newInstance (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultSimJob (eventList, name, requestedServiceTimeMap);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
