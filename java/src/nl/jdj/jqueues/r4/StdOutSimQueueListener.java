package nl.jdj.jqueues.r4;

/** A {@link SimQueueListener} logging events on <code>System.out</code>.
 * 
 * @see StdOutSimQueueVacationListener
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class StdOutSimQueueListener<J extends SimJob, Q extends SimQueue>
implements SimQueueListener<J, Q>
{

  @Override
  public void notifyReset (double oldTime, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + oldTime + ", queue=" + queue + ": RESET.");
  }

  @Override
  public void notifyUpdate (double t, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": UPDATE.");
  }

  @Override
  public void notifyArrival (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": ARRIVAL of job " + job + ".");
  }

  @Override
  public void notifyStart (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": START of job " + job + ".");
  }

  @Override
  public void notifyDrop (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": DROP of job " + job + ".");
  }

  @Override
  public void notifyRevocation (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": REVOCATION of job " + job + ".");
  }

  @Override
  public void notifyDeparture (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": DEPARTURE of job " + job + ".");
  }

  @Override
  public void notifyNewNoWaitArmed (double t, Q queue, boolean noWaitArmed)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": NO_WAIT_ARMED -> " + noWaitArmed + ".");
  }
  
}
