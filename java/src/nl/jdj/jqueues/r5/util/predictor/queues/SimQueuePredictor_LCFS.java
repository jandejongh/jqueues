package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.ArrayList;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link LCFS}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_LCFS<J extends SimJob>
extends SimQueuePredictor_FCFS<J>
{

  @Override
  protected J getJobToStart (final SimQueue queue, final SimQueueState<J, SimQueue> queueState)
  {
    final ArrayList<J> waitingJobs = new ArrayList<> (queueState.getJobsWaitingOrdered ());
    return waitingJobs.get (waitingJobs.size () - 1);
  }

  
  public SimQueuePredictor_LCFS ()
  {
    super ();
  }

}