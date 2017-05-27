package nl.jdj.jqueues.r5.util.loadfactory;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for generating a load (in terms of job arrivals, vacations, etc.) on one or more {@link SimQueue}s.
 * 
 * <p>
 * Load factories play a crucial role in the test packages of this library.
 * 
 * <p>
 * Typically, the factory generates the jobs and appropriate {@link SimEvent}s
 * and schedule the events on a user-supplied {@link SimEventList},
 * but it does not create the queues themselves, nor the {@link SimEventList}.
 * However, this is by no means a requirement.
 * 
 * <p>
 * Currently, this is a tagging interface only.
 * 
 * <p>
 * The {@code pattern} sub-package contains concrete load generators.
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
public interface LoadFactory<J extends SimJob, Q extends SimQueue>
{

}
