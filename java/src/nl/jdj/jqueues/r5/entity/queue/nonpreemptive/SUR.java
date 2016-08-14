package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractClassicSimQueue;
import nl.jdj.jqueues.r5.entity.queue.serverless.WUR;
import nl.jdj.jsimulation.r5.SimEventList;

/** A queueing discipline that takes jobs into service immediately (given server-access credits)
 *  and serves then until the next arrival, at which point they depart.
 * 
 * <p>
 * Serve-Until-Relieved (Serve-Until-Next).
 * 
 * <p>
 * This implementation has infinite buffer size (relevant for server-access credits) and a single server.
 * 
 * <p>
 * This class ignores the requested service-time supplied by a visiting job.
 * Jobs are simply served until the next arrival/start, and the assumption in this queueing system is that the service obtained
 * until the next arrival is <i>exactly</i> the service required.
 * Given that interpretation, this queueing system is non-preemptive.
 * However, we admit that a <i>preemptive</i> interpretation is also possible, given the same results in terms of job visits.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see WUR
 * 
 */
public class SUR
  <J extends SimJob, Q extends SUR>
  extends AbstractClassicSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server SUR queue with infinite buffer size given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public SUR (final SimEventList eventList)
  {
    super (eventList, Integer.MAX_VALUE, 1);
  }

  /** Returns a new {@link SUR} object on the same {@link SimEventList}.
   * 
   * @return A new {@link SUR} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public SUR<J, Q> getCopySimQueue ()
  {
    return new SUR<> (getEventList ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "SUR".
   * 
   * @return "SUR".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "SUR";
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return super.getQoSClass ();
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job at the tail of the job queue.
   * 
   * @see #jobQueue
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Starts the arrived job if server-access credits are available.
   * 
   * @see #hasServerAcccessCredits
   * @see #start
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null || ! getJobsInWaitingArea ().contains (job))
      throw new IllegalArgumentException ();
    if (hasServerAcccessCredits ())
      start (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Removes the job from {@link #jobQueue} and {@link #jobsInServiceArea}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    this.jobQueue.remove (job);
    this.jobsInServiceArea.remove (job);    
  }
  
  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOKATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Removes the job from {@link #jobQueue} and {@link #jobsInServiceArea}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    this.jobQueue.remove (job);
    this.jobsInServiceArea.remove (job);    
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    super.setServerAccessCreditsSubClass ();
  }

  /** Starts jobs as long as there are server-access credits and jobs waiting.
   * 
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #start
   * @see #getFirstJobInWaitingArea
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
      start (time, getFirstJobInWaitingArea ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns <code>true</code>.
   * 
   * @return True.
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return true;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Adds the job to the tail of the service area (which is either first or second position).
   * 
   * <p>
   * Note that we temporarily allow a non-legal state in case a job is already present in the service area:
   * more than one jobs will be in the service area (upon exit) if another job is being served upon entry of this method.
   * We rely on {@link #rescheduleAfterStart} to resolve this.
   * 
   * @see #jobsInServiceArea
   * @see #rescheduleAfterStart
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null || (! getJobs ().contains (job)) || getJobsInServiceArea ().contains (job))
      throw new IllegalArgumentException ();
    if (getJobsInServiceArea ().size () > 1)
      throw new IllegalStateException ();
    this.jobsInServiceArea.add (job);
  }

  /** Departs the first job in {@link #jobsInServiceArea} if it is not the only job in the service area.
   * 
   * <p>
   * Performs sanity checks on the fly.
   * 
   * @see #getFirstJobInServiceArea
   * @see #getFirstJobInServiceArea
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null || (! getJobs ().contains (job)) || (! getJobsInServiceArea ().contains (job)))
      throw new IllegalArgumentException ();
    if (getJobsInServiceArea ().isEmpty () || getJobsInServiceArea ().size () > 2)
      throw new IllegalStateException ();
    if (getJobsInServiceArea ().size () == 2)
      depart (time, getFirstJobInServiceArea ());
    if (getJobsInServiceArea ().size () != 1)
      throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Removes the job from {@link #jobQueue} and {@link #jobsInServiceArea}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    this.jobQueue.remove (departingJob);
    this.jobsInServiceArea.remove (departingJob);    
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
