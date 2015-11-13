package nl.jdj.jqueues.r5.listener;

import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A {@link SimEntityListener} logging events on <code>System.out</code>.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class StdOutSimEntityListener<J extends SimJob, Q extends SimQueue>
implements SimEntityListener<J, Q>
{

  /** Returns the header used in the notifications.
   * 
   * @return {@code this.getClass ().getSimpleName ()}.
   * 
   */
  protected String getHeaderString ()
  {
    return this.getClass ().getSimpleName ();
  }
  
  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("entity=" + entity + ": RESET.");
  }

  @Override
  public void notifyArrival (final double time, final J job, final Q queue)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": ARRIVAL of job " + job + ".");
  }

  @Override
  public void notifyStart (double time, final J job, final Q queue)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": START of job " + job + ".");
  }

  @Override
  public void notifyDrop (final double time, final J job, final Q queue)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": DROP of job " + job + ".");
  }

  @Override
  public void notifyRevocation (final double time, final J job, final Q queue)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": REVOCATION of job " + job + ".");
  }

  @Override
  public void notifyDeparture (final double time, final J job, final Q queue)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", queue=" + queue + ": DEPARTURE of job " + job + ".");
  }

}
