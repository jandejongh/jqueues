package nl.jdj.jqueues.r5.entity.queue.composite.tandem;

import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.AbstractGraySimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.GraySimQueueComposite;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a gray tandem (serial) queue.
 * 
 * <p>
 * In a tandem queue, a (delegate) job visits all sub-queues once in a predetermined sequence.
 * 
 * <p>
 * In a <i>gray</i> tandem queue,
 * jobs first visit the composite gray {@link SimQueue}, then each of the
 * embedded {@link SimQueue}s in a predetermined sequence, and, finally,
 * the composite queue again.
 * 
 * @param <DQ> The (base) type for sub-queues.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see TandemSimQueueSelector
 * @see GrayTandemSimQueue
 * 
 */
public abstract class AbstractGrayTandemSimQueue
  <DQ extends SimQueue, J extends SimJob, Q extends AbstractGrayTandemSimQueue>
  extends AbstractGraySimQueueComposite<DQ, J, Q>
  implements GraySimQueueComposite<DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract gray tandem queue given an event list and a list of queues to put in sequence.
   *
   * @param eventList             The event list to use.
   * @param queues                The queues, an iteration over the set must return (deterministically)
   *                              the non-<code>null</code> queues in intended order of visit.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry.
   * 
   * 
   */
  protected AbstractGrayTandemSimQueue
  (final SimEventList eventList, final Set<DQ> queues)
  {
    super (eventList, queues, new TandemSimQueueSelector<> (queues));
  }

}