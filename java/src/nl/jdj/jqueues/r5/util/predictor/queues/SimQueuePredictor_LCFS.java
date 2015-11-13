package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.ArrayList;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link LCFS}.
 *
 */
public class SimQueuePredictor_LCFS
extends SimQueuePredictor_FCFS
{

  @Override
  protected SimJob getJobToStart (final SimQueue queue, final SimQueueState<SimJob, SimQueue> queueState)
  {
    final ArrayList<SimJob> waitingJobs = new ArrayList<> (queueState.getJobsWaitingOrdered ());
    return waitingJobs.get (waitingJobs.size () - 1);
  }

  
  public SimQueuePredictor_LCFS ()
  {
    super ();
  }

}