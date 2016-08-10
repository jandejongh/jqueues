package nl.jdj.jqueues.r5.extensions.visitscounter;

import java.util.HashMap;
import java.util.Map;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FB_v;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueStateHandler;

/** A {@link SimQueueStateHandler} for counting per-job visits to a {@link SimQueue}.
 *
 * @see SimQueuePredictor_FB_v
 * 
 */
public final class SimQueueVisitsCounterStateHandler
implements SimQueueStateHandler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueStateHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns "SimQueueVisitsCounterStateHandler".
   * 
   * @return "SimQueueVisitsCounterStateHandler".
   * 
   */
  @Override
  public final String getHandlerName ()
  {
    return "SimQueueVisitsCounterStateHandler";
  }

  @Override
  public final void initHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null)
      throw new IllegalArgumentException ();
    if (this.queueState != null)
      throw new IllegalStateException ();
    this.queueState = queueState;
    this.numberOfVisits.clear ();
  }
  
  @Override
  public void resetHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null || queueState != this.queueState)
      throw new IllegalArgumentException ();
    this.numberOfVisits.clear ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT QUEUE STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DefaultSimQueueState queueState = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NUMBER OF VISITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<SimJob, Integer> numberOfVisits = new HashMap<> ();
  
  /** Gets the number of visits recorded for the given job.
   * 
   * @param job The job, non-{@code null}.
   * 
   * @return The number of visits recorded for the given job.
   * 
   * @throws IllegalArgumentException If the job is {@code null} or if the job has not visited the queue (yet).
   * 
   */
  public final int getNumberOfVisits (final SimJob job)
  {
    if (job == null || ! this.numberOfVisits.containsKey (job))
      throw new IllegalArgumentException ();
    return this.numberOfVisits.get (job);
  }
  
  /** Checks whether the given job has visited the queue at least once.
   * 
   * @param job The job, non-{@code null}.
   * 
   * @return {@code true} if the given job has visited the queue at least once.
   * 
   * @throws IllegalArgumentException If the job is {@code null}.
   * 
   */
  public final boolean containsJob (final SimJob job)
  {
    return this.numberOfVisits.containsKey (job);
  }
  
  /** Adds a first-time visiting job, and sets its number of visits to unity.
   * 
   * @param job The job, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the job is {@code null} or if the job has already visited the queue before.
   * 
   */
  public final void newJob (final SimJob job)
  {
    if (job == null)
      throw new IllegalArgumentException ();      
    if (this.numberOfVisits.containsKey (job))
      throw new IllegalArgumentException ();
    this.numberOfVisits.put (job, 1);
  }
  
  /** Increments the number of visits to the queue of the given job.
   * 
   * @param job The job, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the job is {@code null} or if the job has not visited the queue (yet).
   * 
   */
  public final void incNumberOfVisits  (final SimJob job)
  {
    if (job == null)
      throw new IllegalArgumentException ();      
    if (! this.numberOfVisits.containsKey (job))
      throw new IllegalArgumentException ();
    this.numberOfVisits.put (job, this.numberOfVisits.get (job) + 1);
  }

  /** Removes the given job from the internal queue-visits administration, but insist it is known.
   * 
   * @param job The job, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the job is {@code null} or if the job has not visited the queue (yet).
   * 
   */  
  public final void removeJob (final SimJob job)
  {
    if (job == null)
      throw new IllegalArgumentException ();      
    if (! this.numberOfVisits.containsKey (job))
      throw new IllegalArgumentException ();
    this.numberOfVisits.remove (job);
  }
  
}
