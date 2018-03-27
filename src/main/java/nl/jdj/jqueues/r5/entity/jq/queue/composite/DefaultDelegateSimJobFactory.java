package nl.jdj.jqueues.r5.entity.jq.queue.composite;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.qos.DefaultSimJobQoS;

/** A {@link DelegateSimJobFactory} for any (real) {@link SimJob}, as used in composite queues, with support for QoS.
 * 
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
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
public class DefaultDelegateSimJobFactory
<DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
implements DelegateSimJobFactory<AbstractSimJob, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a new {@link AbstractSimJob} or {@link DefaultSimJobQoS} requesting the service time from the real job.
   * 
   * <p>
   * The name has empty (i.e. default) name and is not attached to the underlying event list (there is no need to).
   * 
   * <p>
   * It generates delegate jobs of type {@link AbstractSimJob} that redirect requests for
   * their required service time to a request of the corresponding real job at the real queue.
   * If, however, the real job is supplied with an appropriate QoS structure,
   * it generates a {@link DefaultSimJobQoS} with the same QoS structure, again redirecting requests required service time.
   * 
   * @see AbstractSimJob#getServiceTime
   * @see DefaultSimJobQoS#getServiceTime
   * 
   */
  @Override
  public AbstractSimJob newInstance (final double time, final J job, final Q queue)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (job.getQoSClass () == null || ! Comparable.class.isAssignableFrom (job.getQoSClass ()))
      return new AbstractSimJob (null, null)
      {
        @Override
        public final double getServiceTime (final SimQueue delegateQueue) throws IllegalArgumentException
        {
          // return 0.0;
          return job.getServiceTime (queue);
        }
      };
    else
      // The 0.0 argument refer to the requested service time, but we are bypassing the superclass's mechanism
      // by overriding getServiceTime
      return new DefaultSimJobQoS  (null, null, 0.0, job.getQoSClass (), (Comparable) job.getQoS ())
      {
        @Override
        public final double getServiceTime (final SimQueue delegateQueue) throws IllegalArgumentException
        {
          // return 0.0;
          return job.getServiceTime (queue);
        }
      };
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
