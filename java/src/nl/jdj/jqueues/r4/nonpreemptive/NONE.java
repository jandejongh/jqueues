package nl.jdj.jqueues.r4.nonpreemptive;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link NONE} queue has unlimited waiting capacity, but does not provide
 *  any service.
 *
 * Obviously, the {@link NONE} queue does not schedule any events on the
 * {@link #eventList} and never invokes actions in
 * {@link #startActions} or {@link #departureActions}.
 * It does support job revocations though.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public final class NONE<J extends SimJob, Q extends NONE> extends AbstractNonPreemptiveSimQueue<J, Q>
{

  /** Creates a NONE queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public NONE (final SimEventList eventList)
  {
    super (eventList);
  }
  
  @Override
  protected /* final */ void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected /* final */ void rescheduleAfterArrival (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected /* final */ void rescheduleForNewServerAccessCredits (final double time)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected /* final */ void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Throws {@link IllegalStateException}.
   * 
   * {@inheritDoc}
   * 
   * @throws IllegalStateException Always, as this {@link SimQueue} does not allow departures.
   * 
   */
  @Override
  protected /* final */ void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    throw new IllegalStateException ();
  }

}
