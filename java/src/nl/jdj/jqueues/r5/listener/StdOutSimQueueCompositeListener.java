package nl.jdj.jqueues.r5.listener;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueCompositeListener;

/** A {@link SimQueueCompositeListener} logging events on <code>System.out</code>.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class StdOutSimQueueCompositeListener<J extends SimJob, Q extends SimQueue>
extends StdOutSimQueueListener<J, Q>
implements SimQueueCompositeListener<J, Q>
{

  @Override
  public void notifyPseudoArrival (final double time, final J job, final Q queue)
  {
    if (! isOnlyUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": PSEUDO_ARRIVAL of job " + job + ".");
    }
  }

  @Override
  public void notifyPseudoDrop (final double time, final J job, final Q queue)
  {
    if (! isOnlyUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": PSEUDO_DROP of job " + job + ".");
    }
  }

  @Override
  public void notifyPseudoRevocation (final double time, final J job, final Q queue)
  {
    if (! isOnlyUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": PSEUDO_REVOCATION of job " + job + ".");
    }
  }

  @Override
  public void notifyPseudoAutoRevocation (final double time, final J job, final Q queue)
  {
    if (! isOnlyUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": PSEUDO_AUTO_REVOCATION of job " + job + ".");
    }
  }

  @Override
  public void notifyPseudoStart (double time, final J job, final Q queue)
  {
    if (! isOnlyUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": PSEUDO_START of job " + job + ".");
    }
  }

  @Override
  public void notifyPseudoDeparture (final double time, final J job, final Q queue)
  {
    if (! isOnlyUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": PSEUDO_DEPARTURE of job " + job + ".");
    }
  }

}
