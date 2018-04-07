/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.javades.jqueues.r5.entity.jq.queue.composite;

import java.util.Set;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.parallel.Par;
import org.javades.jqueues.r5.entity.jq.queue.composite.tandem.Tandem;

/** A {@link SimQueue} that embeds a fixed set of other queues;
 *  turning its job visits into visits to the embedded queues.
 * 
 * <p>
 * Also known as a <i>queueing network</i>.
 * Examples are <i>tandem (serial) queues</i>
 * and <i>parallel queues</i>, see {@link Tandem} and {@link Par},
 * respectively.
 * 
 * <p>
 * A composite queue completely hides its internal structure, owns its sub-queues,
 * and uses (1:1) <i>delegate</i> jobs on its sub-queues to represent visiting jobs.
 * Jobs originating from other sources than the composite queue,
 * are <i>not</i> allowed to visit the sub-queues
 * (at the expense of an exception).
 * The "real" and delegate jobs may be of different type.
 * The use of delegate jobs is required
 * because a {@link SimJob} cannot visit multiple queues simultaneously (i.e., the composite queue and one of its sub-queues).
 * 
 * <p>
 * A {@link SimQueueComposite} has several degrees of freedom related to
 * the semantics of the waiting and service areas of a composite queue,
 * and to the notion of starting a job.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see AbstractSimQueueComposite
 * @see AbstractSimQueueComposite_LocalStart
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
public interface SimQueueComposite<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
extends SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (SUB-)QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the set of queues embedded by this {@link SimQueueComposite}.
   * 
   * <p>
   * The set should not be manipulated.
   * Typically, implementations return an unmodifiable set.
   * 
   * <p>
   * Implementations must maintain deterministic ordering of the queues in the set!
   * 
   * @return The non-<code>null</code> set of queues, each non-<code>null</code>.
   * 
   */
  Set<? extends DQ> getQueues ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the sub-queue selector for this {@link SimQueueComposite}.
   * 
   * @return The sub-queue selector for this composite queue.
   * 
   */
  SimQueueSelector<J, DQ> getSimQueueSelector ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DELEGATE SIMJOB FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   * @return The factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   */
  DelegateSimJobFactory<? extends DJ, DQ, J, Q> getDelegateSimJobFactory ();
  
  /** Sets the factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   * @param delegateSimJobFactory The new factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   * @throws IllegalArgumentException      If the argument is {@code null} or otherwise illegal.
   * @throws UnsupportedOperationException If this composite queue does not allow setting the delegate-job factory.
   * 
   */
  void setDelegateSimJobFactory (DelegateSimJobFactory<? extends DJ, DQ, J, Q> delegateSimJobFactory);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
