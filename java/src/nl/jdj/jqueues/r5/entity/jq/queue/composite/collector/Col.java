package nl.jdj.jqueues.r5.entity.jq.queue.composite.collector;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** A composite queue with two queues, a main one and one collecting, upon request,
 *  all dropped, auto-revoked and/or departed jobs from the main queue.
 *
 * <p>
 * The main and collector arguments may be equal.
 * 
 * <p>
 * This queue uses the {@code LocalStart} model as explained with {@link AbstractSimQueueComposite_LocalStart}.
 * 
 * @param <DJ> The delegate-job type.
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
public class Col
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends Col>
  extends AbstractCollectorSimQueue<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a collector queue given an event list, a main queue and a collector queue,
   *  and flags indicating which exit methods to collect.
   *
   * <p>
   * Note that the mainQueue and the collectorQueue arguments may be equal!
   * 
   * @param eventList              The event list to use.
   * @param mainQueue              The main queue.
   * @param collectorQueue         The collector queue.
   * @param collectDrops           Whether to collect drops from the main queue.
   * @param collectAutoRevocations Whether to collect auto-revocations from the main queue.
   * @param collectDepartures      Whether to collect departures from the main queue.
   * @param delegateSimJobFactory  An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  one of or both queues are <code>null</code>.
   * 
   * @see CollectorSimQueueSelector
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public Col
  (final SimEventList eventList,
   final SimQueue<DJ, DQ> mainQueue,
   final SimQueue<DJ, DQ> collectorQueue,
   final boolean collectDrops,
   final boolean collectAutoRevocations,
   final boolean collectDepartures,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, mainQueue, collectorQueue, collectDrops, collectAutoRevocations, collectDepartures, delegateSimJobFactory);
  }

  /** Returns a new {@link Col} object on the same {@link SimEventList} with copies of the main and
   *  collector queues, exit-collection settings and the same delegate-job factory.
   * 
   * @return A new {@link Col} object on the same {@link SimEventList} with copies of the main and
   *         collector queues, exit-collection settings and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the main or collection queues
   *                                       could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getMainQueue
   * @see #getCollectorQueue
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public Col<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> mainQueueCopy = getMainQueue ().getCopySimQueue ();
    final SimQueue<DJ, DQ> collectorQueueCopy = getCollectorQueue ().getCopySimQueue ();
    return new Col<>
      (getEventList (),
       mainQueueCopy,
       collectorQueueCopy,
       isCollectDrops (),
       isCollectAutoRevocations (),
       isCollectAutoRevocations (),
       getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "Col(conditions)[mainQueue-&gt;collectorQueue]".
   * 
   * <p>
   * The {@code conditions} string reflects the
   * the combined values of
   * {@link #isCollectDrops},
   * {@link #isCollectAutoRevocations}
   * and {@link #isCollectDepartures}
   * properties.
   * 
   * @return "Col(conditions)[mainQueue-&gt;collectorQueue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    String conditions = "None";
    boolean first = true;
    if (isCollectDrops ())
    {
      conditions = "Dr";
      first = false;
    }
    if (isCollectAutoRevocations ())
    {
      if (first)
        conditions = "AR";
      else
        conditions = ",AR";
      first = false;
    }
    if (isCollectDepartures ())
    {
      if (first)
        conditions = "De";
      else
        conditions = ",De";
      first = false;
      
    }
    return "Col(" + conditions + ")[" + getMainQueue () + "->" + getCollectorQueue () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
