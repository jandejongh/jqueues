package nl.jdj.jqueues.r2;

import java.util.Iterator;
import java.util.Random;
import nl.jdj.jsimulation.r3.SimEvent;
import nl.jdj.jsimulation.r3.SimEventList;

/** An abstract base class for non-preemptive queueing disciplines
 * for {@link SimJob}s.
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
    public void arrive (final J job, final double time)
    {
      assert job.getQueue () == null;
      assert ! this.jobQueue.contains (job);
      update (time);
      this.jobQueue.add (job);
      job.setQueue (this);
      notifyArrival (time, job);
    }

    @Override
    public boolean revoke
      (final J job,
      final double time,
      final boolean interruptService)
    {
      assert job.getQueue () == this;
      update (time);
      job.setQueue (null);
      boolean found = this.jobQueue.remove (job);
      assert found;
      notifyRevocation (time, job);
      return true;
    }

    @Override
    protected void rescheduleAfterDeparture
      (final J departedJob, final double time)
    {
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
    public void arrive (final J job, final double time)
    {
      assert job.getQueue () == null;
      assert ! this.jobQueue.contains (job);
      update (time);
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
      job.setQueue (this);
      notifyArrival (time, job);
      if (this.jobQueue.size () == 1)
      {
        final SimEvent<J> event
          = new NonPreemptiveQueue.DepartureEvent
          (time + job.getServiceTime (this), job);
        this.eventList.add (event);
        assert this.eventsScheduled.isEmpty ();
        this.eventsScheduled.add (event);
        assert this.jobsExecuting.isEmpty ();
        this.jobsExecuting.add (job);
        notifyStart (time, job);
      }
    }

    @Override
    public boolean revoke
      (final J job,
      final double time,
      final boolean interruptService)
    {
      assert job.getQueue () == this;
      update (time);
      boolean rescheduleNeeded = false;
      if (this.jobsExecuting.contains (job))
      {
        if (! interruptService)
          return false;
        else
        {
          boolean found = this.jobsExecuting.remove (job);
          assert found;
          assert this.eventsScheduled.size () == 1;
          final SimEvent<J> event
            = this.eventsScheduled.iterator ().next ();
          found = this.eventsScheduled.remove (event);
          assert found;
          found = this.eventList.remove (event);
          assert found;
          rescheduleNeeded = true;
        }
      }
      job.setQueue (null);
      final boolean found = this.jobQueue.remove (job);
      assert found;
      if (rescheduleNeeded)
        rescheduleAfterDeparture (job, time);
      notifyRevocation (time, job);
      return true;
    }

    @Override
    protected void rescheduleAfterDeparture
      (final SimJob departedJob, final double time)
    {
      if (! this.jobQueue.isEmpty ())
      {
        final J job = this.jobQueue.get (0);
        final SimEvent<J> event =
          new NonPreemptiveQueue.DepartureEvent
          (time + job.getServiceTime (this), job);
        this.eventList.add (event);
        assert this.eventsScheduled.isEmpty ();
        this.eventsScheduled.add (event);
        assert this.jobsExecuting.isEmpty ();
        this.jobsExecuting.add (job);
        notifyStart (time, job);
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
   * This queueing discipline, unlike e.g., {@link FIFO}, has multiple (actually infinite) servers.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static class IS<J extends SimJob, Q extends IS> extends NonPreemptiveQueue<J, Q> 
  {

    @Override
    public void arrive (final J job, final double time)
    {
      // System.out.println ("Arrival at IS-queue @" + time);
      assert job.getQueue () == null;
      assert ! this.jobQueue.contains (job);
      update (time);
      this.jobQueue.add (job);
      job.setQueue (this);
      final SimEvent<J> event = new NonPreemptiveQueue.DepartureEvent
        (time + job.getServiceTime (this), job);
      this.eventList.add (event);
      this.eventsScheduled.add (event);
      this.jobsExecuting.add (job);
      notifyArrival (time, job);
      notifyStart (time, job);
   }

    @Override
    public boolean revoke
      (final J job,
      final double time,
      final boolean interruptService)
    {
      assert job.getQueue () == this;
      update (time);
      if (! interruptService)
        return false;
      else
      {
        boolean found = this.jobsExecuting.remove (job);
        assert found;
        SimEvent<J> event = null;
        final Iterator<SimEvent<J>> i
          = this.eventsScheduled.iterator ();
        while (i.hasNext ())
        {
          final SimEvent<J> e = i.next ();
          if (e.getObject () == job)
          {
            event = e;
            break;
          }
        }
        found = this.eventsScheduled.remove (event);
        assert found;
        found = this.eventList.remove (event);
        assert found;
      }
      job.setQueue (null);
      final boolean found = this.jobQueue.remove (job);
      assert found;
      notifyRevocation (time, job);
      return true;
    }

    @Override
    protected void rescheduleAfterDeparture
      (final J departedJob, final double time)
    {
    }

    public IS (final SimEventList eventList)
    {
      super (eventList);
    }

  }

}
