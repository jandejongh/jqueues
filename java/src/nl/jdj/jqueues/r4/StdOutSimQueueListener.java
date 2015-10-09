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
  public void update (double t, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": UPDATE.");
  }

  @Override
  public void arrival (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": ARRIVAL of job " + job + ".");
  }

  @Override
  public void start (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": START of job " + job + ".");
  }

  @Override
  public void drop (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": DROP of job " + job + ".");
  }

  @Override
  public void revocation (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": REVOCATION of job " + job + ".");
  }

  @Override
  public void departure (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": DEPARTURE of job " + job + ".");
  }

  @Override
  public void newNoWaitArmed (double t, Q queue, boolean noWaitArmed)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": NO_WAIT_ARMED -> " + noWaitArmed + ".");
  }
  
}
