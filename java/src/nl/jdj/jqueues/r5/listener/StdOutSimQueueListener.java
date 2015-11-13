package nl.jdj.jqueues.r5.listener;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueListener;

/** A {@link SimQueueListener} logging events on <code>System.out</code>.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class StdOutSimQueueListener<J extends SimJob, Q extends SimQueue>
extends StdOutSimEntityListener<J, Q> 
implements SimQueueListener<J, Q>
{

  @Override
  public void notifyUpdate (final double time, final Q queue)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": UPDATE.");
  }

  @Override
  public void notifyNewNoWaitArmed (final double time, final Q queue, final boolean noWaitArmed)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": NO_WAIT_ARMED -> " + noWaitArmed + ".");
  }
  
  @Override
  public void notifyStartQueueAccessVacation (final double time, final Q queue)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": START OF QUEUE-ACCESS VACATION.");
  }

  @Override
  public void notifyStopQueueAccessVacation (final double time, final Q queue)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": END OF QUEUE-ACCESS VACATION.");
  }

  @Override
  public void notifyOutOfServerAccessCredits (final double time, final Q queue)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": OUT OF SERVER-ACCESS CREDITS.");
  }

  @Override
  public void notifyRegainedServerAccessCredits (final double time, final Q queue)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": REGAINED SERVER-ACCESS CREDITS.");
  }
  
}
