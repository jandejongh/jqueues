package nl.jdj.jqueues.r5.entity.queue.qos;

import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.extensions.qos.SimQueueQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of {@link SimQueueQoS} based upon {@link AbstractSimQueue}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 * 
 */
public abstract class AbstractSimQueueQoS<J extends SimJob, Q extends AbstractSimQueueQoS, P extends Comparable>
extends AbstractSimQueue<J, Q>
implements SimQueueQoS<J, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link AbstractSimQueueQoS}.
   * 
   * @param eventList     The event list to use, non-{@code null}.
   * @param qosClass      The Java class to use for QoS behavior, non-{@code null}.
   * @param defaultJobQoS The default QoS value to use for non-QoS jobs, non-{@code null}. 
   * 
   * @throws IllegalArgumentException If any of the arguments is <code>null</code>.
   * 
   */
  protected AbstractSimQueueQoS (final SimEventList eventList, final Class<P> qosClass, final P defaultJobQoS)
  {
    super (eventList);
    if (qosClass == null || defaultJobQoS == null)
      throw new IllegalArgumentException ();
    this.qosClass = qosClass;
    this.defaultJobQoS = defaultJobQoS;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Class<P> qosClass;

  @Override
  public final Class<? extends P> getQoSClass ()
  {
    return this.qosClass;
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void setQoSClass (final Class<? extends P> qosClass)
  {
    SimQueueQoS.super.setQoSClass (qosClass);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns {@code null}, since the QoS value of a queue has no meaning.
   * 
   * @return {@code null}.
   * 
   */
  @Override
  public final P getQoS ()
  {
    return null;
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void setQoS (final P qos)
  {
    SimQueueQoS.super.setQoS (qos);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (DEFAULT) JOB QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final P defaultJobQoS;

  @Override
  public final P getDefaultJobQoS ()
  {
    return this.defaultJobQoS;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: RESET ENTITY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.jobsQoSMap.clear ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOB QoS MAP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected final NavigableMap<P, Set<J>> jobsQoSMap = new TreeMap<> ();
  
  @Override
  public final NavigableMap<P, Set<J>> getJobsQoSMap ()
  {
    return this.jobsQoSMap;
  }
  
  /** Gets the job in the waiting area that is next to serve.
   * 
   * <p>
   * Iterates over the job sets in increasing order of QoS value,
   * and iterates over the jobs within each set in order as enforced by the standard Java {@link Set} iterator,
   * and returns the first job it finds that is <i>not</i> in {@link #jobsInServiceArea}.
   * 
   * <p>
   * This method does (some) sanity checks on {@link #jobsQoSMap} on the fly.
   * 
   * @return The job in the waiting area that is next to serve, {@code null} if there are no waiting jobs.
   * 
   * @throws IllegalStateException If the {@link #jobsQoSMap} is in an illegal state.
   * 
   * @see #jobsQoSMap
   * @see #jobQueue
   * @see #jobsInServiceArea
   * 
   */
  protected final J getNextJobToServeInWaitingArea ()
  {
    for (final Set<J> jobsP: jobsQoSMap.values ())
      if (jobsP == null || jobsP.isEmpty ())
        throw new IllegalStateException ();
      else
        for (final J job : jobsP)
          if (job == null || ! this.jobQueue.contains (job))
            throw new IllegalStateException ();
          else if (! this.jobsInServiceArea.contains (job))
            return job;
    return null;
  }
  
}
