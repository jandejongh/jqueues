package nl.jdj.jqueues.r5.misc.example;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.qos.DefaultSimJobQoS;
import nl.jdj.jqueues.r5.extensions.qos.SimJobQoS;
import nl.jdj.jqueues.r5.listener.StdOutSimEntityListener;
import nl.jdj.jsimulation.r5.SimEventList;

/** Implementation of {@link SimJobQoS} used (as starting point) in (most of) the examples.
 * 
 * <p>
 * Each job has a public index 'n', set upon construction.
 * 
 * @param <J> The job type.
 * @param <Q> The queue type for jobs.
 * @param <P> The type used for QoS.
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
public class DefaultExampleSimJobQoS<J extends DefaultExampleSimJobQoS, Q extends SimQueue, P extends Comparable>
extends DefaultSimJobQoS<J, Q, P>
{
 
  /** Whether or not this job reports main queue operations to {@link System#out}
   *  through a {@link StdOutSimEntityListener}.
   * 
   */
  private final boolean reported;
  
  /** The index number of this job (not used except for reporting).
   * 
   */
  private final int n;
  
  /** Creates a new {@link DefaultExampleSimJobQoS}.
   * 
   * <p>
   * The {@link SimJob} created is <i>not</i> attached to a {@link SimEventList} (i.e., it does not receive reset events
   * from the event list, nor does it have to; subclasses may override this).
   * 
   * @param reported             Whether or not this job reports main queue operations to {@link System#out}.
   * @param n                    The index of the job (only used for reporting).
   * @param requestedServiceTime See {@link DefaultSimJob#DefaultSimJob}.
   * @param qosClass             The Java class to use for QoS behavior, non-{@code null}.
   * @param qos                  The QoS value for this job, non-{@code null}. 
   * 
   */
  public DefaultExampleSimJobQoS (final boolean reported,
    final int n,
    final double requestedServiceTime,
    final Class<P> qosClass,
    final P qos)
  {
    super (null, Integer.toString (n), requestedServiceTime, qosClass, qos);
    this.reported = reported;
    this.n = n;
    if (this.reported)
      registerSimEntityListener (new StdOutSimEntityListener ());
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }

  /** Returns {@code "DefaultExampleSimJobQoS[index]"}.
   * 
   * @return {@code "DefaultExampleSimJobQoS[index]"}.
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "DefaultExampleSimJobQoS[" + this.n + "]";
  }
  
}
