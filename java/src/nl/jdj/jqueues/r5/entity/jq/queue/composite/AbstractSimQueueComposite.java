package nl.jdj.jqueues.r5.entity.jq.queue.composite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor.Processor;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link SimQueueComposite}.
 * 
 * <p>
 * This abstract base class registers the sub-queues and the queue selector,
 * and takes care of all administration related to job visits,
 * creating delegate jobs and maintaining the mapping between real and delegate jobs.
 * It registers a {@link MultiSimQueueNotificationProcessor}
 * on the sub-queues,
 * and registers the (abstract) method {@link #processSubQueueNotifications}
 * as its processor.
 * It also registers itself as a {@link SimEntityListener} on the sub-queues
 * in order to capture their updates.
 * 
 * <p>
 * Despite all the work performed in this abstract base class,
 * many degrees of freedom still exits for concrete implementation,
 * because only compliance with {@link SimQueueComposite}
 * is targeted.
 * See {@link AbstractSimQueueComposite_LocalStart}
 * for a more complete (and restricted in flexibility) implementation.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
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
public abstract class AbstractSimQueueComposite
<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractSimQueueComposite>
extends AbstractSimQueue<J, Q>
implements SimQueueComposite<DJ, DQ, J, Q>,
  SimEntityListener, SimQueueSelector<J, DQ>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract network of queues.
   * 
   * <p>
   * After calling the super constructor,
   * this methods sets the delegate job factory,
   * and inhibits future automatic resets from the event list on all sub-queues through {@link SimQueue#setIgnoreEventListReset},
   * since this object will take care of that (and depends on the absence of "independent" resets
   * of the sub-queues).
   * In addition, it registers as a {@link SimEntityListener} on each sub-queue
   * (only required for UPDATE notifications).
   * It then creates a single {@link MultiSimQueueNotificationProcessor} for all sub-queues,
   * and registers method {@link #processSubQueueNotifications} as its processor.
   * Finally, it resets the local part of the object through a (private) variant of {@link #resetEntitySubClass}
   * that does not invoke its super method.
   * This, among others, will reset the sub-queues.
   * 
   * @param eventList             The event list to be shared between this queue and the embedded queues.
   * @param queues                A set holding the "embedded" queues.
   * @param simQueueSelector      The object for routing jobs through the network of embedded queues;
   *                                if {@code null}, no sub-queues will be visited.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * 
   * @throws IllegalArgumentException If the event list is {@code null},
   *                                    the <code>queue</code> argument is <code>null</code>, has <code>null</code> members,
   *                                    or contains this composite queue.
   * 
   * @see SimQueueSelector
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see SimEntity#setIgnoreEventListReset
   * @see SimEntity#registerSimEntityListener
   * @see MultiSimQueueNotificationProcessor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see #processSubQueueNotifications
   * @see #resetEntitySubClass
   * 
   */
  protected AbstractSimQueueComposite
  (final SimEventList eventList,
   final Set<DQ> queues,
   final SimQueueSelector<J, DQ> simQueueSelector,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList);
    if (queues == null || queues.contains (null) || queues.contains ((DQ) this))
      throw new IllegalArgumentException ();
    this.queues = queues;
    this.simQueueSelector = simQueueSelector;
    this.delegateSimJobFactory =
      ((delegateSimJobFactory == null) ? new DefaultDelegateSimJobFactory () : delegateSimJobFactory);
    for (final DQ q : this.queues)
    {
      q.setIgnoreEventListReset (true);
      // We still need to register as listener, despite the multi-queue event processor,
      // in order to obtain UPDATE notifications from the sub-queues.
      q.registerSimEntityListener (this);
    }
    final MultiSimQueueNotificationProcessor<DJ, DQ>  subQueueEventProcessor =
      new MultiSimQueueNotificationProcessor<> (getQueues ());
    subQueueEventProcessor.setProcessor (this::processSubQueueNotifications);
    resetEntitySubClassLocal ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (SUB)QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The queues, all non-null.
   * 
   * Set is also non-null and final.
   * 
   */
  private final Set<DQ> queues;
  
  @Override
  public final Set<DQ> getQueues ()
  {
    return this.queues;
  }

  /** Returns the index of a given queue in a set.
   * 
   * <p>
   * Note that elements in a {@link Set} are unordered,
   * unless the specific implementation (like {@link LinkedHashSet}) takes care of that.
   * Be careful with this method!
   * 
   * @param queues The set, must be non-{@code null}.
   * @param queue  The queue; must be present in the set and non-{@code null}.
   * 
   * @return The index of the queue in the set (by iteration order).
   * 
   * @throws IllegalArgumentException If the set or the queue is {@code null},
   *                                  or if the queue is not present in the set.
   * 
   * @param <DQ> The queue-type.
   * 
   */
  public static <DQ extends SimQueue> int getIndex (final Set<DQ> queues, final DQ queue)
  {
    if (queues == null || queue == null || ! queues.contains (queue))
      throw new IllegalArgumentException ();
    final Iterator<DQ> iterator = queues.iterator ();
    for (int q = 0; q < queues.size (); q++)
      if (iterator.next () == queue)
        return q;
    throw new IllegalArgumentException ();
  }
  
  /** Returns the index of given sub-queue.
   * 
   * @param queue The sub-queue; must be present in {@link #getQueues}.
   * 
   * @return The index of the sub-queue in {@link #getQueues}.
   * 
   * @throws IllegalArgumentException If the <code>queue</code> is <code>null</code> or not present in {@link #getQueues}.
   * 
   */
  protected final int getIndex (final DQ queue)
  {
    return AbstractSimQueueComposite.getIndex (getQueues (), queue);
  }
  
  /** Returns a queue in a set by its index.
   * 
   * <p>
   * Note that elements in a {@link Set} are unordered,
   * unless the specific implementation (like {@link LinkedHashSet}) takes care of that.
   * Be careful with this method!
   * 
   * @param queues The queues.
   * @param q      The index.
   * 
   * @return The queue in the set with given index (by iteration order).
   * 
   * @throws IllegalArgumentException If the set is {@code null}
   *                                  or the index is (strictly) negative or larger or equal than the size of the set.
   *
   * @param <DQ> The queue-type.
   * 
   */
  public static <DQ extends SimQueue> DQ getQueue (final Set<DQ> queues, final int q)
  {
    if (queues == null || q < 0 || q >= queues.size ())
      throw new IllegalArgumentException ();
    final Iterator<DQ> iterator = queues.iterator ();
    int i = 0;
    DQ dq = iterator.next ();
    while (i < q)
    {
      i++;
      dq = iterator.next ();
    }
    return dq;    
  }
  
  /** Returns a sub-queue by its index.
   * 
   * @param q The index.
   * 
   * @return The (sub-)queue in {@link #getQueues} with given index.
   * 
   * @throws IllegalArgumentException If the index is (strictly) negative or larger or equal than the size of {@link #getQueues}.
   * 
   */
  protected final DQ getQueue (final int q)
  {
    return AbstractSimQueueComposite.getQueue (getQueues (), q);
  }
  
  /** Returns a copy of a given set of {@link SimQueue}s, each of which is copied in itself.
   * 
   * @param queues The set of {@link SimQueue}s.
   * 
   * @return A copy of the given set of {@link SimQueue}s, each of which is copied in itself.
   * 
   * @see SimQueue#getCopySimQueue
   * 
   * @throws IllegalArgumentException      If <code>queues == null </code> or contains a <code>null</code> element.
   * @throws UnsupportedOperationException If copying any of the sub-queues is unsupported; this should be considered as a
   *                                       software error.
   * 
   */
  public static Set<SimQueue> getCopySimQueues (final Set<SimQueue> queues) throws UnsupportedOperationException
  {
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    final Set<SimQueue> set = new LinkedHashSet<> ();
    for (final SimQueue q : queues)
      set.add (q.getCopySimQueue ());
    return set;    
  }
  
  /** Returns a copy of the set of sub-queues, each of which is copied in itself.
   * 
   * @return A copy of the set of sub-queues, each of which is copied in itself.
   * 
   * @see #getQueues
   * @see SimQueue#getCopySimQueue
   * 
   * @throws UnsupportedOperationException If copying any of the sub-queues is unsupported; this should be considered as a
   *                                       software error.
   * 
   */
  protected final Set<DQ> getCopySubSimQueues () throws UnsupportedOperationException
  {
    final Set<DQ> set = new LinkedHashSet<> ();
    for (final DQ dq : getQueues ())
      set.add ((DQ) dq.getCopySimQueue ());
    return set;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The (user-supplied) {@link SimQueueSelector} for selecting the sub-queue to visit.
   * 
   */
  private final SimQueueSelector<J, DQ> simQueueSelector;
  
  /** Returns the (user-supplied) {@link SimQueueSelector} for selecting the sub-queue to visit.
   * 
   * @return The {@link SimQueueSelector} for selecting the sub-queue to visit.
   * 
   */
  @Override
  public final SimQueueSelector<J, DQ> getSimQueueSelector ()
  {
    return this.simQueueSelector;
  }

  /** Does nothing.
   * 
   */
  @Override
  public final void resetSimQueueSelector ()
  {
  }

  /** Selects the first sub-queue to visit from the user-supplied {@link SimQueueSelector},
   *  or none if no such object was supplied.
   * 
   * @param time The time of arrival of the job.
   * @param job  The job, non-<code>null</code>.
   * 
   * @return The first sub-queue to visit, if <code>null</code>, the job is to depart immediately.
   *         If no selector is supplied upon construction, this method returns {@code null}.
   * 
   * @see #getSimQueueSelector
   * 
   */
  @Override
  public final DQ selectFirstQueue (final double time, final J job)
  {
    if (this.simQueueSelector != null && this.simQueueSelector != this)
      return this.simQueueSelector.selectFirstQueue (time, job);
    else
      return null;
  }
  
  /** Selects the next sub-queue to visit from the user-supplied {@link SimQueueSelector},
   *  or none if no such object was supplied.
   * 
   * @param time          The current time, i.e., the departure time of the job at its previous queue.
   * @param job           The job, non-<code>null</code>.
   * @param previousQueue The previous queue the job visited, and just departed from.
   * 
   * @return The next sub-queue to visit, if <code>null</code>, the job is to depart immediately.
   *         If no selector is supplied upon construction, this method returns {@code null}.
   * 
   * @see #getSimQueueSelector
   * 
   */
  @Override
  public final DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (this.simQueueSelector != null && this.simQueueSelector != this)
      return this.simQueueSelector.selectNextQueue (time, job, previousQueue);
    else
      return null;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DELEGATE SIMJOBS AND REAL/DELEGATE SIMJOB MAPPINGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   */
  private DelegateSimJobFactory<? extends DJ, DQ, J, Q> delegateSimJobFactory;
  
  @Override
  public final DelegateSimJobFactory<? extends DJ, DQ, J, Q> getDelegateSimJobFactory ()
  {
    return this.delegateSimJobFactory;
  }
  
  @Override
  public final void setDelegateSimJobFactory (final DelegateSimJobFactory<? extends DJ, DQ, J, Q> delegateSimJobFactory)
  {
    if (delegateSimJobFactory == null)
      throw new IllegalArgumentException ();
    this.delegateSimJobFactory = delegateSimJobFactory;
  }
  
  /** Maps "real" jobs onto delegate jobs.
   * 
   * Kept in sync with {@link realSimJobMap}.
   * 
   */
  private final Map<J, DJ> delegateSimJobMap = new HashMap<> ();
  
  /** Maps delegate jobs onto "real" jobs.
   * 
   * Kept in sync with {@link realSimJobMap}.
   * 
   */
  private final Map<DJ, J> realSimJobMap = new HashMap<> ();

  /** Checks for the presence of a real job (in the administration of this composite queue).
   * 
   * <p>
   * The result of this method is equivalent to {@code this.jobQueue.contains (realJob)},
   * with additional sanity checks on the internal administration,
   * in particular on the mapping between real and delegate jobs.
   * 
   * <p>
   * This method does not check the presence of the delegate job on a sub-queue.
   * 
   * @param realJob The real {@link SimJob}; if {@code null}, false is returned.
   * 
   * @return {@code True} if and only if the argument is a real job currently visiting this composite queue.
   * 
   * @throws IllegalStateException If sanity checks on the internal job administrations fail.
   * 
   * @see #jobQueue
   * 
   */
  protected final boolean isRealJob (final J realJob)
  {
    if (realJob == null)
      return false;
    if (this.jobQueue.contains (realJob))
    {
      final DJ delegateJob = this.delegateSimJobMap.get (realJob);
      if (delegateJob == null)
        throw new IllegalStateException ();
      if (this.realSimJobMap.get (delegateJob) != realJob)
        throw new IllegalStateException ();
      return true;
    }
    else
      return false;
  }
  
  /** Checks for the presence of a delegate job (in the administration of this composite queue).
   * 
   * <p>
   * This method performs sanity checks on the internal administration,
   * in particular on the mapping between real and delegate jobs.
   * 
   * @param delegateJob The delegate {@link SimJob}; if {@code null}, false is returned.
   * 
   * @return {@code True} if and only if the argument is a delegate job of a real job currently visiting this composite queue.
   * 
   * @throws IllegalStateException If sanity checks on the internal job administrations fail.
   * 
   * @see #getDelegateJob
   * 
   */
  protected final boolean isDelegateJob (final DJ delegateJob)
  {
    if (delegateJob == null)
      return false;
    if (this.realSimJobMap.containsKey (delegateJob))
    {
      final J realJob = this.realSimJobMap.get (delegateJob);
      if (realJob == null)
        throw new IllegalStateException ();
      if (this.delegateSimJobMap.get (realJob) != delegateJob)
        throw new IllegalStateException ();
      if (! this.jobQueue.contains (realJob))
        throw new IllegalStateException ();
      return true;
    }
    else
      return false;
  }
  
  /** Returns the delegate job for given real job.
   * 
   * Performs various sanity checks on the argument and the internal administration consistency.
   * 
   * @param realJob The real job.
   * 
   * @return The delegate job.
   * 
   * @throws IllegalStateException If sanity checks fail.
   * 
   * @see #isRealJob
   * @see #isDelegateJob
   * 
   */
  protected final DJ getDelegateJob (final J realJob)
  {
    if (realJob == null)
      throw new IllegalStateException ();
    if (! this.jobQueue.contains (realJob))
      throw new IllegalStateException ();
    final DJ delegateJob = this.delegateSimJobMap.get (realJob);
    if (delegateJob == null)
      throw new IllegalStateException ();
    if (this.realSimJobMap.get (delegateJob) != realJob)
      throw new IllegalStateException ();
    return delegateJob;
  }

  /** Returns the real job for given delegate job, and asserts its presence on the given (sub-)queue, or on no (sub-)queue at all.
   * 
   * <p>
   * By using this method, you assume that the delegate job is present on the given sub-{@code queue},
   * or, if passing a {@code null} argument for the {@code queue},
   * on no sub-queue at all.
   * This method will rigorously check your assumption and happily throw an {@link IllegalStateException}
   * if your assumption proves wrong.
   * Clearly, this method is primarily intended for internal consistency checking.
   * 
   * <p>
   * Performs various additional sanity checks on the arguments and the internal administration consistency.
   * 
   * @param delegateJob The delegate job.
   * @param queue       The queue at which the delegate job currently resides,
   *                    {@code null} if it is supposed to reside on <i>none</i> of the (sub-)queues..
   * 
   * @return The real job.
   * 
   * @throws IllegalStateException If sanity checks fail, including the case where a corresponding real job could not be found,
   *                               or where assumption on the delegate-job whereabout proves to be wrong.
   * 
   * @see #getRealJob(SimJob)
   * 
   */
  protected final J getRealJob (final DJ delegateJob, final DQ queue)
  {
    if (delegateJob == null || (queue != null && ! getQueues ().contains (queue)))
      throw new IllegalStateException ();
    final J realJob = this.realSimJobMap.get (delegateJob);
    if (realJob == null)
      throw new IllegalStateException ();
    if (this.delegateSimJobMap.get (realJob) != delegateJob)
      throw new IllegalStateException ();
    if (! this.jobQueue.contains (realJob))
      throw new IllegalStateException ();
    if (queue == null)
    {
      for (final DQ subQueue : getQueues ())
        if (subQueue.getJobs ().contains (delegateJob))
          throw new IllegalStateException ();
    }
    else if (! queue.getJobs ().contains (delegateJob))
      throw new IllegalStateException ();
    return realJob;
  }
  
  /** Returns the real job for given delegate job.
   * 
   * <p>
   * Performs various sanity checks on the arguments and the internal administration consistency.
   * 
   * <p>
   * This method does <i>not</i> check the presence (nor absence) of the delegate job on a (or any) sub-queue.
   * 
   * @param delegateJob The delegate job.
   * 
   * @return The real job.
   * 
   * @throws IllegalStateException If sanity checks fail, including the case where a corresponding real job could not be found.
   * 
   * @see #getRealJob(SimJob, SimQueue)
   * 
   */
  protected final J getRealJob (final DJ delegateJob)
  {
    if (delegateJob == null)
      throw new IllegalStateException ();
    final J realJob = this.realSimJobMap.get (delegateJob);
    if (realJob == null)
      throw new IllegalStateException ();
    if (this.delegateSimJobMap.get (realJob) != delegateJob)
      throw new IllegalStateException ();
    if (! this.jobQueue.contains (realJob))
      throw new IllegalStateException ();
    return realJob;
  }
  
  /** Adds a real job, creating its delegate job.
   * 
   * @param realJob The real job, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the real job is already present,
   *                                  or if no delegate job for it could be created.
   * @throws IllegalStateException    If the internal administration is found inconsistent.
   * 
   */
  protected final void addRealJobLocal (final J realJob)
  {
    if (realJob == null)
      throw new IllegalArgumentException ();
    if (this.jobQueue.contains (realJob) || this.jobsInServiceArea.contains (realJob))
      throw new IllegalArgumentException ();
    if (this.delegateSimJobMap.containsKey (realJob))
      throw new IllegalStateException ();
    if (this.realSimJobMap.containsValue (realJob))
      throw new IllegalStateException ();
    final DJ delegateSimJob = this.delegateSimJobFactory.newInstance (getLastUpdateTime (), realJob, (Q) this);
    if (delegateSimJob == null)
      throw new IllegalArgumentException ();
    this.delegateSimJobMap.put (realJob, delegateSimJob);
    this.realSimJobMap.put (delegateSimJob, realJob);
    this.jobQueue.add (realJob);
  }

  /** Removes a real job and a delegate job from the internal data structures.
   * 
   * <p>
   * The jobs do not have to be present; if not, this method has (with respect to that job) no effect.
   * 
   * <p>
   * This method does <i>not</i> remove the delegate job from a sub-queue it might currently be visiting.
   * 
   * @param realJob     The real job     (may be {@code null} meaning no real job is to be removed).
   * @param delegateJob The delegate job (may be {@code null} meaning no delegate job is to be removed).
   * 
   */
  protected final void removeJobsFromQueueLocal (final J realJob, final DJ delegateJob)
  {
    this.jobQueue.remove (realJob);
    this.jobsInServiceArea.remove (realJob);
    this.delegateSimJobMap.remove (realJob);
    if (delegateJob != null)
      this.realSimJobMap.remove (delegateJob);
  }
  
  /** A mapper between real and delegate jobs (for use by sub-classes).
   * 
   * @param <J>  The job type.
   * @param <DJ> The delegate-job type.
   * 
   */
  public interface RealDelegateJobMapper<J extends SimJob, DJ extends SimJob>
  {
    
    /** Returns the delegate job for a real job.
     * 
     * @param realJob The real job.
     * 
     * @return The delegate job.
     * 
     * @throws IllegalArgumentException If the real job argument is {@code null} or not found.
     * 
     */
    DJ getDelegateJob (J realJob);
    
    /** Returns the real job for a delegate job.
     * 
     * @param delegateJob The delegate job.
     * 
     * @return The real job.
     * 
     * @throws IllegalArgumentException If the delegate job argument is {@code null} or not found.
     * 
     */
    J getRealJob (DJ delegateJob);
    
  }
  
  /** A mapper object for mapping real and delegate jobs of this composite queue (for use by sub-classes).
   * 
   */
  protected final RealDelegateJobMapper realDelegateJobMapper = new RealDelegateJobMapper<J, DJ> ()
  {
    
    @Override
    public final DJ getDelegateJob (final J realJob)
    {
      return AbstractSimQueueComposite.this.getDelegateJob (realJob);
    }

    @Override
    public final J getRealJob (final DJ delegateJob)
    {
      return AbstractSimQueueComposite.this.getRealJob ((DJ) delegateJob);
    }
    
  };
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets this {@link AbstractSimQueueComposite}.
   * 
   * <p>
   * Calls super method (not if called from constructor, for which a private variant for local resets is used),
   * resets the sub-queue selector,
   * clears the internal mapping between real and delegate {@link SimJob}s (removing all real and delegate jobs),
   * and resets all sub-queues in the order in which they appear in {@link #getQueues}.
   * (Note: some sub-classes rely on this order!)
   * 
   * @see #removeJobFromQueueUponRevokation
   * @see #rescheduleAfterRevokation
   * @see SimQueue#resetEntity
   * @see SimQueueSelector#resetSimQueueSelector
   * @see SimQueue#setServerAccessCredits
   * @see SimQueue#isStartArmed
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    // Implemented in private method to make it accessible from the constructor(s).
    resetEntitySubClassLocal ();
  }
  
  private void resetEntitySubClassLocal ()
  {
    //
    // NOTE: This method is invoked from the constructor instead of resetEntitySubClass ().
    //
    this.simQueueSelector.resetSimQueueSelector ();
    this.delegateSimJobMap.clear ();
    this.realSimJobMap.clear ();
    for (final DQ q : getQueues ())
      q.resetEntity ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Processes the pending atomic notification from the sub-queues.
   * 
   * <p>
   * Core method for reacting to {@link SimEntityListener#notifyStateChanged} notifications from all sub-queues.
   * This method is registered as the processor for an anonymous {@link MultiSimQueueNotificationProcessor}
   * (for all sub-queues) created upon construction,
   * see {@link Processor}
   * and {@link MultiSimQueueNotificationProcessor#setProcessor}.
   * 
   * @param notifications The sub-queue notifications, will be modified; empty upon return.
   * 
   * @throws IllegalArgumentException If the list is {@code null} or empty, or contains a notification from another queue
   *                                  than the a sub-queue,
   *                                  or if other sanity checks fail.
   * 
   * @see MultiSimQueueNotificationProcessor
   * @see Processor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * 
   */
  protected abstract void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls {@link #update} in order to update our own time in response to an increase in time on one of the sub-queues.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or not one of our sub-queues.
   * @throws IllegalStateException    If time is in the past.
   * 
   * @see #getQueues
   * @see #update
   * @see #getLastUpdateTime
   * 
   */
  @Override
  public final void notifyUpdate (final double time, final SimEntity entity)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalArgumentException ();
    update (time);
  }
  
  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyResetEntity (final SimEntity entity)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override  
  public final void notifyStateChanged
  (final double time,
   final SimEntity entity,
   final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> notifications)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
