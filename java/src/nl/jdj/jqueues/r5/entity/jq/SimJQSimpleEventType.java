package nl.jdj.jqueues.r5.entity.jq;

import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue.AutoRevocationPolicy;

/** Simple representations of events on both {@link SimJob}s <i>and</i> {@link SimQueue}s.
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
public interface SimJQSimpleEventType
extends SimEntitySimpleEventType
{
  
  /** A job arrival.
   * 
   * @see SimQueue#arrive
   * @see SimJQEvent.Arrival
   * 
   */
  public static Member ARRIVAL = new Member ("ARRIVAL");
  
  /** A job drop.
   * 
   * @see SimJQEvent.Drop
   * 
   */
  public static Member DROP = new Member ("DROP");
  
  /** A job revocation.
   * 
   * @see SimQueue#revoke
   * @see SimJQEvent.Revocation
   * 
   */
  public static Member REVOCATION = new Member ("REVOCATION");
  
  /** A job auto-revocation.
   * 
   * @see AutoRevocationPolicy
   * @see SimJQEvent.AutoRevocation
   * 
   */
  public static Member AUTO_REVOCATION = new Member ("AUTO_REVOCATION");
  
  /** A job start.
   * 
   * @see SimJQEvent.Start
   * 
   */
  public static Member START = new Member ("START");
  
  /** A job departure.
   * 
   * @see SimJQEvent.Departure
   * 
   */
  public static Member DEPARTURE = new Member ("DEPARTURE");
  
}
