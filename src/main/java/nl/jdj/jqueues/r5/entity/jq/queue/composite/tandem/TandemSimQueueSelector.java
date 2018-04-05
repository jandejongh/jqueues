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
package nl.jdj.jqueues.r5.entity.jq.queue.composite.tandem;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;

/** A {@link SimQueueSelector} for tandem queues.
 * 
 * <p>
 * In a tandem queue, a (delegate) job visits all sub-queues once in a predetermined sequence.
 * This {@link SimQueueSelector} is mandatory for {@link Tandem}.
 * 
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
 *
 * @see Tandem
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
public class TandemSimQueueSelector<J extends SimJob, DQ extends SimQueue>
implements SimQueueSelector<J, DQ>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link SimQueueSelector} for a tandem queue.
   * 
   * @param queues The queues, must be non-{@code null}.
   * 
   * @throws IllegalArgumentException If the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry.
   * 
   */
  public TandemSimQueueSelector (final Set<DQ> queues)
  {
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    this.queues = new LinkedHashSet<> (queues);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<DQ> queues;  
   
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing.
   * 
   */
  @Override
  public final void resetSimQueueSelector ()
  {
  }
  
  /** Returns the first {@link SimQueue} to visit by a (delegate) {@link SimJob}.
   * 
   * @return The first {@link SimQueue} returned by an iterator over the queue set,
   *         or <code>null</code> if that set is {@code null} or empty.
   * 
   */
  @Override
  public final DQ selectFirstQueue (final double time, final J job)
  {
    return ((this.queues == null || this.queues.isEmpty ()) ? null : this.queues.iterator ().next ());
  }
      
  /** Returns the next {@link SimQueue} to visit by a delegate {@link SimJob}.
   * 
   * @return The next {@link SimQueue} after the <code>previousQueue</code> in an iterator
   *         over the queue set,
   *         or <code>null</code> if that set is {@code null} or if no such element exists
   *         (i.e., <code>previousQueue</code> is the last element returned from the iterator).
   * 
   * @throws IllegalStateException If the previous queue argument is <code>null</code> or not a member of the queue set.
   * 
   */
  @Override
  public final DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (this.queues == null || this.queues.isEmpty ())
      throw new IllegalStateException ();
    if (previousQueue == null)
      throw new IllegalStateException ();
    final Iterator<DQ> iterator = this.queues.iterator ();
    boolean found = false;
    while (iterator.hasNext () && ! found)
      found = (iterator.next () == previousQueue);
    if (! found)
      throw new IllegalStateException ();
    return (iterator.hasNext () ? iterator.next () : null);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
