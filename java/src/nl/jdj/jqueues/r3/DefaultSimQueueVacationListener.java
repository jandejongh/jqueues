package nl.jdj.jqueues.r3;

/** A convenience implementation (all methods are empty) of {@link SimQueueVacationListener}.
 *
 */
public class DefaultSimQueueVacationListener<J extends SimJob, Q extends SimQueue>
extends DefaultSimQueueListener<J, Q>
implements SimQueueVacationListener<J, Q>
{

  @Override
  public void notifyStartQueueAccessVacation (double t, Q queue)
  {
  }

  @Override
  public void notifyStopQueueAccessVacation (double t, Q queue)
  {
  }
  
}
