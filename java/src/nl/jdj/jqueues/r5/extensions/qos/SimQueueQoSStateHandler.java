package nl.jdj.jqueues.r5.extensions.qos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueStateHandler;

/** A {@link SimQueueStateHandler} for {@link SimQueueQoS}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 * 
 */
public final class SimQueueQoSStateHandler<J extends SimJob, Q extends SimQueueQoS, P>
implements SimQueueStateHandler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates a new handler.
   * 
   * <p>
   * Creates and maintains the mapping from QoS values onto jobs present with that QoS value.
   * Depending upon the parameter, a {@link HashMap} ({@code false}) or a {@link TreeMap} ({@code true}) is used.
   * 
   * @param comparableQoS Whether the underlying QoS class to support is {@link Comparable}.
   * 
   * @see #getJobsQoSMap
   * 
   */
  public SimQueueQoSStateHandler (final boolean comparableQoS)
  {
    if (comparableQoS)
      this.jobsQoSMap = new TreeMap<> ();
    else
      this.jobsQoSMap = new HashMap<> ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueStateHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns "SimQueueQoSHandler".
   * 
   * @return "SimQueueQoSHandler".
   * 
   */
  @Override
  public final String getHandlerName ()
  {
    return "SimQueueQoSHandler";
  }

  @Override
  public final void initHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null)
      throw new IllegalArgumentException ();
    if (this.queueState != null)
      throw new IllegalStateException ();
    this.queueState = queueState;
    this.jobsQoSMap.clear ();
  }
  
  @Override
  public void resetHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null || queueState != this.queueState)
      throw new IllegalArgumentException ();
    this.jobsQoSMap.clear ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT QUEUE STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DefaultSimQueueState<J, Q> queueState = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: JOBS QoS MAP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final Map<P, Set<J>> jobsQoSMap;
  
  /** Maps QoS values onto jobs present with that QoS value.
   * 
   * <p>
   * Mapping is 1 to many. Empty and {@code null} sets are not allowed.
   * A job can be present in at most one of the value sets.
   * 
   * <p>
   * See the constructor of this handler for details about finer structuring of the map (e.g., when caller may assume
   * it is a {@link NavigableMap}.
   * 
   * @return A mapping of QoS values onto jobs present with that QoS value.
   * 
   */
  public final Map<P, Set<J>> getJobsQoSMap ()
  {
    return this.jobsQoSMap;
  }
  
  public void updateJobsQoSMap ()
  {
    final Iterator<Entry<P, Set<J>>> entryIterator = this.jobsQoSMap.entrySet ().iterator ();
    while (entryIterator.hasNext ())
    {
      final Set<J> jobs = entryIterator.next ().getValue ();
      if (jobs == null || jobs.isEmpty ())
        throw new IllegalStateException ();
      final Iterator<J> jobIterator = jobs.iterator ();
      while (jobIterator.hasNext ())
        if (! this.queueState.getJobs ().contains (jobIterator.next ()))
          jobIterator.remove ();
      if (jobs.isEmpty ())
        entryIterator.remove ();
    }
    final P defaultQoS = (P) this.queueState.getQueue ().getDefaultJobQoS ();
    if (defaultQoS == null)
      throw new IllegalStateException ();
    for (final J job : this.queueState.getJobs ())
    {
      final P qos = (job.getQoS () == null ? defaultQoS : ((P) job.getQoS ()));
      if (! this.jobsQoSMap.containsKey (qos))
        this.jobsQoSMap.put (qos, new LinkedHashSet<> ());
      if (! this.jobsQoSMap.get (qos).contains (job))
        this.jobsQoSMap.get (qos).add (job);
    }
  }
  
}
