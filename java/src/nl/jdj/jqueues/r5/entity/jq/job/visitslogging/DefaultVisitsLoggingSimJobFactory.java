package nl.jdj.jqueues.r5.entity.jq.job.visitslogging;

import java.util.Map;
import nl.jdj.jqueues.r5.entity.jq.job.SimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for {@link DefaultVisitsLoggingSimJob}s.
 *
 * @param <Q>  The queue type for jobs.
 * 
 * @see DefaultVisitsLoggingSimJob
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
public class DefaultVisitsLoggingSimJobFactory<Q extends SimQueue>
implements SimJobFactory<DefaultVisitsLoggingSimJob, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a new {@link DefaultVisitsLoggingSimJob} with given parameters.
   * 
   * @return A new {@link DefaultVisitsLoggingSimJob} with given parameters.
   * 
   * @see DefaultVisitsLoggingSimJob#DefaultVisitsLoggingSimJob
   * 
   */
  @Override
  public DefaultVisitsLoggingSimJob newInstance
  (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultVisitsLoggingSimJob (eventList, name, requestedServiceTimeMap);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
