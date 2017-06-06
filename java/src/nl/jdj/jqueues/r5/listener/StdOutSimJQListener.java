package nl.jdj.jqueues.r5.listener;

import nl.jdj.jqueues.r5.entity.jq.SimJQListener;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** A {@link SimJQListener} logging events on <code>System.out</code>.
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
public class StdOutSimJQListener<J extends SimJob, Q extends SimQueue>
extends StdOutSimEntityListener
implements SimJQListener<J, Q>
{

  private boolean onlyResetsAndUpdatesAndStateChanges = false;

  /** Returns whether this object only reports resets, updates and state changes.
   * 
   * @return Whether this object only reports resets, updates and state changes.
   * 
   * @see #notifyResetEntity
   * @see #notifyUpdate
   * @see #notifyStateChanged
   * 
   */
  public final boolean isOnlyResetsAndUpdatesAndStateChanges ()
  {
    return this.onlyResetsAndUpdatesAndStateChanges;
  }
 
  /** Sets whether this object only reports resets, updates and state changes.
   * 
   * @param onlyResetsAndUpdatesAndStateChanges Whether this object only reports resets, updates and state changes.
   * 
   * @see #notifyResetEntity
   * @see #notifyUpdate
   * @see #notifyStateChanged
   * 
   */
  public final void setOnlyResetsAndUpdatesAndStateChanges (final boolean onlyResetsAndUpdatesAndStateChanges)
  {
    this.onlyResetsAndUpdatesAndStateChanges = onlyResetsAndUpdatesAndStateChanges;
  }
  
  @Override
  public void notifyArrival (final double time, final J job, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": ARRIVAL of job " + job + ".");
    }
  }

  @Override
  public void notifyStart (double time, final J job, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": START of job " + job + ".");
    }
  }

  @Override
  public void notifyDrop (final double time, final J job, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": DROP of job " + job + ".");
    }
  }

  @Override
  public void notifyRevocation (final double time, final J job, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": REVOCATION of job " + job + ".");
    }
  }

  @Override
  public void notifyAutoRevocation (final double time, final J job, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": AUTO_REVOCATION of job " + job + ".");
    }
  }

  @Override
  public void notifyDeparture (final double time, final J job, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": DEPARTURE of job " + job + ".");
    }
  }

}