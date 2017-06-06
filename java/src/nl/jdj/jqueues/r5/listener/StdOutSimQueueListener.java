package nl.jdj.jqueues.r5.listener;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueListener;

/** A {@link SimQueueListener} logging events on <code>System.out</code>.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
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
public class StdOutSimQueueListener<J extends SimJob, Q extends SimQueue>
extends StdOutSimJQListener<J, Q> 
implements SimQueueListener<J, Q>
{

  @Override
  public void notifyStartQueueAccessVacation (final double time, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": START OF QUEUE-ACCESS VACATION.");
    }
  }

  @Override
  public void notifyStopQueueAccessVacation (final double time, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": END OF QUEUE-ACCESS VACATION.");
    }
  }

  @Override
  public void notifyOutOfServerAccessCredits (final double time, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": OUT OF SERVER-ACCESS CREDITS.");
    }
  }

  @Override
  public void notifyRegainedServerAccessCredits (final double time, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": REGAINED SERVER-ACCESS CREDITS.");
    }
  }
  
  @Override
  public void notifyNewStartArmed (final double time, final Q queue, final boolean startArmed)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": START_ARMED -> " + startArmed + ".");
    }
  }
  
}
