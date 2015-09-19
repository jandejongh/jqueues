package nl.jdj.jqueues.r4;

/** A {@link SimQueueVacationListener} logging events on <code>System.out</code>.
 * 
 * @see StdOutSimQueueListener
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class StdOutSimQueueVacationListener<J extends SimJob, Q extends SimQueue>
extends StdOutSimQueueListener<J, Q>
implements SimQueueVacationListener<J, Q>
{

  @Override
  public void notifyStartQueueAccessVacation (double t, Q queue)
  {
    System.out.println ("[StdOutSimQueueVacationListener] t=" + t + ", queue=" + queue + ": START OF QUEUE-ACCESS VACATION.");
  }

  @Override
  public void notifyStopQueueAccessVacation (double t, Q queue)
  {
    System.out.println ("[StdOutSimQueueVacationListener] t=" + t + ", queue=" + queue + ": END OF QUEUE-ACCESS VACATION.");
  }

  @Override
  public void notifyOutOfServerAccessCredits (double t, Q queue)
  {
    System.out.println ("[StdOutSimQueueVacationListener] t=" + t + ", queue=" + queue + ": OUT OF SERVER-ACCESS CREDITS.");
  }

  @Override
  public void notifyRegainedServerAccessCredits (double t, Q queue)
  {
    System.out.println ("[StdOutSimQueueVacationListener] t=" + t + ", queue=" + queue + ": REGAINED SERVER-ACCESS CREDITS.");
  }
  
}