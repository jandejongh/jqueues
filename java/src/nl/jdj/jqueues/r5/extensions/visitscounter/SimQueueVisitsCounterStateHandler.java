package nl.jdj.jqueues.r5.extensions.visitscounter;

import java.util.HashMap;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FB_v;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_Pattern;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueStateHandler;

/** A {@link SimQueueStateHandler} for counting per-job visits to a {@link SimQueue}.
 *
 * @see SimQueuePredictor_FB_v
 * @see SimQueuePredictor_Pattern
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
    this.totalNumberOfVisits = 0;
    this.numberOfVisitsPerJob.clear ();
  }
  
  @Override
  public void resetHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null || queueState != this.queueState)
      throw new IllegalArgumentException ();
    this.totalNumberOfVisits = 0;
    this.numberOfVisitsPerJob.clear ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT QUEUE STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DefaultSimQueueState queueState = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TOTAL NUMBER OF VISITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private int totalNumberOfVisits = 0;
  
  public final int getTotalNumberOfVisits ()
  {
    return this.totalNumberOfVisits;
  }
  
  public final void incTotalNumberOfVisits ()
  {
    this.totalNumberOfVisits++;
  }
  
  public final void resetTotalNumberOfVisits ()
  {
    this.totalNumberOfVisits = 0;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NUMBER OF VISITS PER JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<SimJob, Integer> numberOfVisitsPerJob = new HashMap<> ();
  
  /** Gets the number of visits recorded for the given job.
   * 
   * @param job The job, non-{@code null}.
   * 
   * @return The number of visits recorded for the given job.
   * 
   * @throws IllegalArgumentException If the job is {@code null} or if the job has not visited the queue (yet).
   * 
   */
  public final int getNumberOfVisitsForJob (final SimJob job)
  {
    if (job == null || ! this.numberOfVisitsPerJob.containsKey (job))
      throw new IllegalArgumentException ();
    return this.numberOfVisitsPerJob.get (job);
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
    return this.numberOfVisitsPerJob.containsKey (job);
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
    if (this.numberOfVisitsPerJob.containsKey (job))
      throw new IllegalArgumentException ();
    this.numberOfVisitsPerJob.put (job, 1);
  }
  
  /** Increments the number of visits to the queue of the given job.
   * 
   * @param job The job, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the job is {@code null} or if the job has not visited the queue (yet).
   * 
   */
  public final void incNumberOfVisitsForJob  (final SimJob job)
  {
    if (job == null)
      throw new IllegalArgumentException ();      
    if (! this.numberOfVisitsPerJob.containsKey (job))
      throw new IllegalArgumentException ();
    this.numberOfVisitsPerJob.put (job, this.numberOfVisitsPerJob.get (job) + 1);
  }

  /** Removes the given job from the internal queue-visits administration, but insists it is known.
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
    if (! this.numberOfVisitsPerJob.containsKey (job))
      throw new IllegalArgumentException ();
    this.numberOfVisitsPerJob.remove (job);
  }
  
}
