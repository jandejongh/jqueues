package nl.jdj.jqueues.r5.util.loadfactory;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** Abstract implementation of {@link LoadFactory_SQ_SV}, mostly meant to store utility methods common to
 *  concrete implementations.
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
public abstract class AbstractLoadFactory_SQ_SV<J extends SimJob, Q extends SimQueue>
implements LoadFactory_SQ_SV<J, Q>
{
  
}
