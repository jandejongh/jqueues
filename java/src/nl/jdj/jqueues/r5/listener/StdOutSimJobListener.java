package nl.jdj.jqueues.r5.listener;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJobListener;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** A {@link SimJobListener} logging events on <code>System.out</code>.
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
public class StdOutSimJobListener<J extends SimJob, Q extends SimQueue>
extends StdOutSimEntityListener<J, Q>
implements SimJobListener<J, Q>
{

}
