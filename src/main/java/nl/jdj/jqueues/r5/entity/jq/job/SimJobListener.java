package nl.jdj.jqueues.r5.entity.jq.job;

import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimJQListener;

/** A listener to state changes of one or multiple {@link SimJob}s.
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
public interface SimJobListener<J extends SimJob, Q extends SimQueue>
extends SimJQListener<J, Q>
{
  
  /* EMPTY */
 
}
