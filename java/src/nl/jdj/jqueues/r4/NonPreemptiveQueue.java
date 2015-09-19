package nl.jdj.jqueues.r4;

import java.util.Random;
import nl.jdj.jsimulation.r4.SimEventList;

/** An abstract base class for non-preemptive queueing disciplines
 *  for {@link SimJob}s.
 *
 * The class and all implementations support job revocations, but not drops (infinite queue length).
 * 
 * <p>This abstract class relies heavily on the partial {@link SimQueue} implementation of {@link AbstractSimQueue}.
 * 
 * <p>All concrete subclasses of {@link NonPreemptiveQueue} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimEventList
 * @see SimEventList#run
 * 
 */
public abstract class NonPreemptiveQueue<J extends SimJob, Q extends NonPreemptiveQueue>
  extends AbstractSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  /** Creates a non-preemptive queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  protected NonPreemptiveQueue (final SimEventList eventList)
  {
    super (eventList);
  }

  /** The {@link NONE} queue has unlimited waiting capacity, but does not provide
   *  any service.
   *
   * Obviously, the {@link NONE} queue does not schedule any events on the
   * {@link #eventList} and never invokes actions in
   * {@link #startActions} or {@link #departureActions}.
   * It does support job revocations though.
   *
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static class NONE<J extends SimJob, Q extends NONE> extends NonPreemptiveQueue<J, Q>
  {

    @Override
    protected void insertJobInQueueUponArrival (J job, double time)
    {
      this.jobQueue.add (job);      
    }

    @Override
    protected void rescheduleAfterArrival (J job, double time)
    {
      /* EMPTY */
    }

    @Override
    protected void rescheduleForNewServerAccessCredits (double time)
    {
      /* EMPTY */
    }

    @Override
    protected void removeJobFromQueueUponDrop (J job, double time)
    {
      this.jobQueue.remove (job);
    }

    @Override
    protected void rescheduleAfterDrop (J job, double time)
    {
      /* EMPTY */
    }
    
    @Override
    public boolean removeJobFromQueueUponRevokation (J job, double time, boolean interruptService)
    {
      this.jobQueue.remove (job);
      return true;
    }

    @Override
    protected void rescheduleAfterRevokation (J job, double time)
    {
      /* EMPTY */
    }

    @Override
    protected void removeJobFromQueueUponDeparture (J departingJob, double time)
    {
      throw new IllegalStateException ();
    }
    
    @Override
    protected void rescheduleAfterDeparture
      (final J departedJob, final double time)
    {
      throw new IllegalStateException ();
    }

    public NONE (final SimEventList eventList)
    {
      super (eventList);
    }

  }
  
  /** The {@link FIFO} queue serves jobs in order of arrival times.
   * 
   * First In First Out, also known as First Come First Served (FCFS).
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static class FIFO <J extends SimJob, Q extends FIFO> extends NonPreemptiveQueue<J, Q>
  {

    @Override
    protected void insertJobInQueueUponArrival (J job, double time)
    {
      if (this instanceof LIFO)
        this.jobQueue.add (0, job);
      else if (this instanceof RANDOM)
      {
        final int newPosition
          = ((RANDOM) this).RNG.nextInt (this.jobQueue.size () + 1);
        this.jobQueue.add (newPosition, job);
      }
      else if (this instanceof SJF)
      {
        int newPosition = 0;
        while (newPosition < this.jobQueue.size ()
          && this.jobQueue.get (newPosition).getServiceTime (this) <= job.getServiceTime (this))
          newPosition++;
        this.jobQueue.add (newPosition, job);   
      }
      else if (this instanceof LJF)
      {
        int newPosition = 0;
        while (newPosition < this.jobQueue.size ()
          && this.jobQueue.get (newPosition).getServiceTime (this) >= job.getServiceTime (this))
          newPosition++;
        this.jobQueue.add (newPosition, job);   
      }
      else
        this.jobQueue.add (job);
    }

    @Override
    protected void rescheduleAfterArrival (J job, double time)
    {
      if (! this.jobQueue.contains (job))
        throw new IllegalStateException ();
      if (this.jobQueue.size () == 1)
      {
        if (! this.jobsExecuting.isEmpty ())
          throw new IllegalStateException ();
        if (hasServerAcccessCredits ())
        {
          takeServerAccessCredit ();
          this.jobsExecuting.add (job);
          scheduleDepartureEvent (time + job.getServiceTime (this), job);
          fireStart (time, job);
        }
      }
    }
    
    @Override
    protected void rescheduleForNewServerAccessCredits (double time)
    {
      if (this.jobsExecuting.isEmpty () && ! this.jobQueue.isEmpty ())
        rescheduleAfterDeparture (null, time);
    }

    @Override
    protected void removeJobFromQueueUponDrop (J job, double time)
    {
      removeJobFromQueueUponRevokation (job, time, true);
    }

    @Override
    protected void rescheduleAfterDrop (J job, double time)
    {
      rescheduleAfterRevokation (job, time);
    }

    @Override
    protected boolean removeJobFromQueueUponRevokation (J job, double time, boolean interruptService)
    {
      if (! this.jobQueue.contains (job))
        throw new IllegalStateException ();
      if (this.jobsExecuting.contains (job))
      {
        if (! interruptService)
          return false;
        else
        {
          this.jobsExecuting.remove (job);
          cancelDepartureEvent (job);
        }
      }
      this.jobQueue.remove (job);
      return true;
    }

    @Override
    protected void rescheduleAfterRevokation (J job, double time)
    {
      if (this.jobsExecuting.isEmpty ())
        rescheduleAfterDeparture (job, time);
    }

    @Override
    protected void removeJobFromQueueUponDeparture (J departingJob, double time)
    {
      if (! this.jobQueue.contains (departingJob))
        throw new IllegalStateException ();
      if (! this.jobsExecuting.contains (departingJob))
        throw new IllegalStateException ();
      this.jobQueue.remove (departingJob);
      this.jobsExecuting.remove (departingJob);
    }
    
    @Override
    protected void rescheduleAfterDeparture
      (final SimJob departedJob, final double time)
    {
      if (! (this.eventsScheduled.isEmpty () && this.jobsExecuting.isEmpty ()))
        throw new IllegalStateException ();
      if ((! this.jobQueue.isEmpty ()) && hasServerAcccessCredits ())
      {
        takeServerAccessCredit ();
        final J job = this.jobQueue.get (0);
        this.jobsExecuting.add (job);
        scheduleDepartureEvent (time + job.getServiceTime (this), job);
        fireStart (time, job);
      }
    }

    public FIFO (final SimEventList eventList)
    {
      super (eventList);
    }

  }
  
  /** An alias for {@link FIFO}.
   * 
   * First-Come, First Served.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see FIFO
   * 
   */
  public static class FCFS <J extends SimJob, Q extends FCFS> extends FIFO<J, Q>
  {
    
    public FCFS (final SimEventList eventList)
    {
      super (eventList);
    }

  }

  /** The {@link LIFO} queue serves jobs in reverse order of arrival times.
   * 
   * Last In First Out, also known as Last Come First Served (LCFS).
   * Note that this is the non-preemptive version of the queueing discipline:
   * Once a job is taken into service, it is not preempted in favor of a new arrival.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static class LIFO<J extends SimJob, Q extends LIFO> extends FIFO<J, Q>
  {

    public LIFO (final SimEventList eventList)
    {
      super (eventList);
    }

  }

  /** An alias for {@link LIFO}.
   * 
   * Last-Come, First Served.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see LIFO
   * 
   */
  public static class LCFS <J extends SimJob, Q extends LCFS> extends LIFO<J, Q>
  {
    
    public LCFS (final SimEventList eventList)
    {
      super (eventList);
    }

  }

  /** The {@link RANDOM} queue serves jobs in random order.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static class RANDOM<J extends SimJob, Q extends RANDOM> extends FIFO<J, Q>
  {

    protected final Random RNG;

    public RANDOM (final SimEventList eventList)
    {
      this (eventList, null);
    }

    public RANDOM (final SimEventList eventList, final Random RNG)
    {
      super (eventList);
      this.RNG = ((RNG == null) ? new Random () : RNG);
    }

  }

  /** The {@link SJF} queue serves jobs in order of ascending requested service times.
   * 
   * Shortest-Job First.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see SimJob#getServiceTime
   * 
   */
  public static class SJF<J extends SimJob, Q extends SJF> extends FIFO<J, Q>
  {

    public SJF (final SimEventList eventList)
    {
      super (eventList);
    }

  }

  /** The {@link LJF} queue serves jobs in order of descending requested service times.
   * 
   * Longest-Job First.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see SimJob#getServiceTime
   * 
   */
  public static class LJF<J extends SimJob, Q extends LJF> extends FIFO<J, Q>
  {

    public LJF (final SimEventList eventList)
    {
      super (eventList);
    }

  }

  /** The {@link IS} queue serves all jobs simultaneously.
   * 
   * Infinite Server.
   * 
   * <p>
   * This queueing discipline, unlike e.g., {@link FIFO}, has multiple (actually infinite) servers.
   * 
   * <p>
   * In the presence of vacations, i.e., jobs are not immediately admitted to the servers,
   * this implementation respects the arrival order of jobs.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static class IS<J extends SimJob, Q extends IS> extends NonPreemptiveQueue<J, Q> 
  {

    @Override
    protected void insertJobInQueueUponArrival (J job, double time)
    {
      this.jobQueue.add (job);
    }

    @Override
    protected void rescheduleAfterArrival (J job, double time)
    {
      if (hasServerAcccessCredits ())
      {
        takeServerAccessCredit ();
        this.jobsExecuting.add (job);
        if (this instanceof IC)
          scheduleDepartureEvent (time, job);
        else
          scheduleDepartureEvent (time + job.getServiceTime (this), job);
        fireStart (time, job);
      }
    }

    @Override
    protected void rescheduleForNewServerAccessCredits (double time)
    {
      while (this.jobsExecuting.size () < this.jobQueue.size () && hasServerAcccessCredits ())
        rescheduleAfterArrival (this.jobQueue.get (0), time);
    }

    @Override
    protected void removeJobFromQueueUponDrop (J job, double time)
    {
      // Be carefull here; job may not have started yet due to queue-access vacation.
      if (this.jobsExecuting.contains (job))
        removeJobFromQueueUponRevokation (job, time, true);
    }

    @Override
    protected void rescheduleAfterDrop (J job, double time)
    {
    }

    @Override
    protected boolean removeJobFromQueueUponRevokation (J job, double time, boolean interruptService)
    {
      if (! interruptService)
        return false;
      if (! this.jobsExecuting.contains (job))
        throw new IllegalStateException ();
      cancelDepartureEvent (job);
      this.jobsExecuting.remove (job);
      this.jobQueue.remove (job);
      return true;
    }

    @Override
    protected void rescheduleAfterRevokation (J job, double time)
    {
      /* EMPTY */
    }
    
    @Override
    protected void removeJobFromQueueUponDeparture (J departingJob, double time)
    {
      if (! this.jobQueue.contains (departingJob))
        throw new IllegalStateException ();
      if (! this.jobsExecuting.contains (departingJob))
        throw new IllegalStateException ();
      this.jobQueue.remove (departingJob);
      this.jobsExecuting.remove (departingJob);
    }
    
    @Override
    protected void rescheduleAfterDeparture
      (final J departedJob, final double time)
    {
      /* EMPTY */
    }

    public IS (final SimEventList eventList)
    {
      super (eventList);
    }

  }

  /** The {@link IC} queue serves all jobs in zero time.
   * 
   * Infinite Capacity.
   * 
   * <p>
   * This queueing discipline guarantees that it will never invoke {@link SimJob#getServiceTime}.
   * 
   * <p>
   * In the presence of vacations, i.e., jobs are not immediately admitted to service,
   * this implementation respects the arrival order of jobs.
   * 
   * <p>
   * For jobs with identical arrival times, it is <i>not</i> guaranteed that they will depart in order of arrival.
   * In that case, a job may even depart before the arrival of another job (should the underlying {@link SimEventList} not
   * respect insertion order).
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static class IC<J extends SimJob, Q extends IS> extends IS<J, Q> 
  {
    
    public IC (final SimEventList eventList)
    {
      super (eventList);
    }
    
  }
  
}