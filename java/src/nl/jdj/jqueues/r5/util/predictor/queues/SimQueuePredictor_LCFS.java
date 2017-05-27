package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.ArrayList;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link LCFS}.
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
public class SimQueuePredictor_LCFS
extends SimQueuePredictor_FCFS
{

  @Override
  protected SimJob getJobToStart (final SimQueue queue, final SimQueueState<SimJob, SimQueue> queueState)
  {
    final ArrayList<SimJob> waitingJobs = new ArrayList<> (queueState.getJobsInWaitingAreaOrdered ());
    return waitingJobs.get (waitingJobs.size () - 1);
  }

  
  public SimQueuePredictor_LCFS ()
  {
    super ();
  }

  @Override
  public String toString ()
  {
    return "Predictor[LCFS]";
  }

}