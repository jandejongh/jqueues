package nl.jdj.jsimulation.v1.jobqueue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import nl.jdj.jsimulation.v1.SimEvent;
import nl.jdj.jsimulation.v1.SimEventAction;
import nl.jdj.jsimulation.v1.SimEventQueue;

/** An abstract base class for non-preemptive queueing disciplines
 * for {@link SimJob}s.
 *
 * The class and all implementations support job revokations.
 *
 */
public abstract class NonPreemptiveQueue
  implements SimQueue
{

  /** The underlying event queue for {@link SimQueue} operations
   *  (to be supplied and fixed in the constructor).
   *
   */
  protected final SimEventQueue eventQueue;

  /** Jobs currently in queue.
   *
   */
  protected final List<SimJob> jobQueue = new Vector<SimJob> ();

  /** Jobs currently being executed by the server.
   *
   */
  protected final Set<SimJob> jobsExecuting
    = new HashSet<SimJob> ();

  /** Events scheduled on behalf of this {@link SimQueue}.
   *
   */
  protected final Set<SimEvent<SimJob>> eventsScheduled
    = new HashSet<SimEvent<SimJob>> ();

  protected double lastEventTime = Double.NEGATIVE_INFINITY;

  protected final List<SimEventAction> arrivalActions
    = new Vector<SimEventAction> ();

  @Override
  public void addArrivalAction (final SimEventAction action)
  {
    if (action == null)
    {
      return;
    }
    if (this.arrivalActions.contains (action))
    {
      return;
    }
    this.arrivalActions.add (action);
  }

  @Override
  public void removeArrivalAction (final SimEventAction action)
  {
    this.arrivalActions.remove (action);
  }

  protected final List<SimEventAction> startActions
    = new Vector<SimEventAction> ();

  @Override
  public void addStartAction (final SimEventAction action)
  {
    if (action == null)
    {
      return;
    }
    if (this.startActions.contains (action))
    {
      return;
    }
    this.startActions.add (action);
  }

  @Override
  public void removeStartAction (final SimEventAction action)
  {
    this.startActions.remove (action);
  }

  protected final List<SimEventAction> departureActions
    = new Vector<SimEventAction> ();

  @Override
  public void addDepartureAction (final SimEventAction action)
  {
    if (action == null)
    {
      return;
    }
    if (this.departureActions.contains (action))
    {
      return;
    }
    this.departureActions.add (action);
  }

  @Override
  public void removeDepartureAction (final SimEventAction action)
  {
    this.departureActions.remove (action);
  }

  protected abstract void rescheduleAfterDeparture
    (SimJob departedJob, double time);

  protected class DepartureEvent extends SimEvent<SimJob>
  {
    public DepartureEvent
      (final double time,
      final SimJob job)
    {
      super (time, job, NonPreemptiveQueue.this.DEPARTURE_ACTION);
    }
  }

  /** A {@link SimEventAction} that is invoked when a job departs from the queue.
   *
   * This action takes care of administration of the internal data, i.e.,
   * clearing the job's queue {@link SimJob#setQueue},
   * removing it from the {@link #jobQueue}
   * and the {@link #jobsExecuting} lists,
   * and updating {@link #eventsScheduled}.
   * It then invokes the discipline-specific {@link #rescheduleAfterDeparture}
   * method, the job's departure action {@link SimJob#getQueueDepartAction}
   * (if present) and the queue's departure actions {@link #departureActions},
   * in that order.
   * 
   */
  protected final SimEventAction<SimJob> DEPARTURE_ACTION
    = new SimEventAction<SimJob> ()
          {
            @Override
            public void action
              (final SimEvent event)
            {
              final double time = event.getTime ();
              // System.out.println ("Departure from queue @" + time);
              final SimJob job = (SimJob) event.getObject ();
              assert job.getQueue () == NonPreemptiveQueue.this;
              assert time >= NonPreemptiveQueue.this.lastEventTime;
              NonPreemptiveQueue.this.lastEventTime = time;
              job.setQueue (null);
              boolean found;
              found = NonPreemptiveQueue.this.jobQueue.remove (job);
              assert found;
              found = NonPreemptiveQueue.this.jobsExecuting.remove (job);
              assert found;
              found = NonPreemptiveQueue.this.eventsScheduled.remove (event);
              assert found;
              NonPreemptiveQueue.this.rescheduleAfterDeparture (job, time);
              for (SimEventAction action: NonPreemptiveQueue.this.departureActions)
              {
                action.action (event);
              }
              final SimEventAction<SimJob> dAction = job.getQueueDepartAction ();
              if (dAction != null)
              {
                dAction.action (event);
              }
            }
          };

  /** Create a non-preemptive queue given an event queue.
   *
   * @param eventQueue The event queue to use.
   *
   */
  protected NonPreemptiveQueue (final SimEventQueue eventQueue)
  {
    this.eventQueue = eventQueue;
  }

  /** The {@link NONE} queue has unlimited waiting capacity, but does not provide
   *  any service.
   *
   * Obviously, the {@link NONE} queue does not schedule any events on the
   * {@link #eventQueue} and never invokes actions in
   * {@link #startActions} or {@link #departureActions}.
   *
   */
  public static class NONE extends NonPreemptiveQueue
  {

    @Override
    public void arrive (final SimJob job, final double time)
    {
      assert job.getQueue () == null;
      assert ! this.jobQueue.contains (job);
      assert time >= this.lastEventTime;
      this.lastEventTime = time;
      this.jobQueue.add (job);
      job.setQueue (this);
      for (SimEventAction<SimJob> action: this.arrivalActions)
      {
        action.action (new SimEvent (time, job, action));
      }
      final SimEventAction<SimJob> aAction = job.getQueueArriveAction ();
      if (aAction != null)
      {
        aAction.action (new SimEvent (time, job, aAction));
      }
    }

    @Override
    public boolean revoke
      (final SimJob job,
      final double time,
      final boolean interruptService)
    {
      assert job.getQueue () == this;
      assert time >= this.lastEventTime;
      this.lastEventTime = time;
      job.setQueue (null);
      boolean found = this.jobQueue.remove (job);
      assert found;
      final SimEventAction<SimJob> rAction = job.getQueueRevokeAction ();
      if (rAction !=  null)
      {
        rAction.action (new SimEvent<SimJob> (time, job, rAction));
      }
      return true;
    }

    @Override
    protected void rescheduleAfterDeparture
      (final SimJob departedJob, final double time)
    {
    }

    public NONE (final SimEventQueue eventQueue)
    {
      super (eventQueue);
    }


  }

  public static class FIFO extends NonPreemptiveQueue
  {

    @Override
    public void arrive (final SimJob job, final double time)
    {
      assert job.getQueue () == null;
      assert ! this.jobQueue.contains (job);
      assert time >= this.lastEventTime;
      this.lastEventTime = time;
      if (this instanceof LIFO)
      {
        this.jobQueue.add (0, job);
      }
      else if (this instanceof RANDOM)
      {
        final int newPosition
          = ((RANDOM) this).RNG.nextInt (this.jobQueue.size () + 1);
        this.jobQueue.add (newPosition, job);
      }
      else
      {
        this.jobQueue.add (job);
      }
      job.setQueue (this);
      for (SimEventAction<SimJob> action: this.arrivalActions)
      {
        action.action (new SimEvent (time, job, action));
      }
      final SimEventAction<SimJob> aAction = job.getQueueArriveAction ();
      if (aAction != null)
      {
        aAction.action (new SimEvent (time, job, aAction));
      }
      if (this.jobQueue.size () == 1)
      {
        final SimEvent<SimJob> event
          = new NonPreemptiveQueue.DepartureEvent
          (time + job.getServiceTime (this), job);
        this.eventQueue.add (event);
        assert this.eventsScheduled.isEmpty ();
        this.eventsScheduled.add (event);
        assert this.jobsExecuting.isEmpty ();
        this.jobsExecuting.add (job);
        for (SimEventAction<SimJob> action: this.startActions)
        {
          action.action (new SimEvent (time, job, action));
        }
        final SimEventAction sAction = job.getQueueStartAction ();
        if (sAction != null)
        {
          sAction.action (new SimEvent (time, job, sAction));
        }
      }
    }

    @Override
    public boolean revoke
      (final SimJob job,
      final double time,
      final boolean interruptService)
    {
      assert job.getQueue () == this;
      assert time >= this.lastEventTime;
      this.lastEventTime = time;
      boolean rescheduleNeeded = false;
      if (this.jobsExecuting.contains (job))
      {
        if (! interruptService)
        {
          return false;
        }
        else
        {
          boolean found = this.jobsExecuting.remove (job);
          assert found;
          assert this.eventsScheduled.size () == 1;
          final SimEvent<SimJob> event
            = this.eventsScheduled.iterator ().next ();
          found = this.eventsScheduled.remove (event);
          assert found;
          found = this.eventQueue.remove (event);
          assert found;
          rescheduleNeeded = true;
        }
      }
      job.setQueue (null);
      final boolean found = this.jobQueue.remove (job);
      assert found;
      if (rescheduleNeeded)
      {
        rescheduleAfterDeparture (job, time);
      }
      final SimEventAction<SimJob> rAction = job.getQueueRevokeAction ();
      if (rAction !=  null)
      {
        rAction.action (new SimEvent<SimJob> (time, job, rAction));
      }
      return true;
    }

    @Override
    protected void rescheduleAfterDeparture
      (final SimJob departedJob, final double time)
    {
      if (! this.jobQueue.isEmpty ())
      {
        final SimJob job = this.jobQueue.get (0);
        final SimEvent<SimJob> event =
          new NonPreemptiveQueue.DepartureEvent
          (time + job.getServiceTime (this), job);
        this.eventQueue.add (event);
        assert this.eventsScheduled.isEmpty ();
        this.eventsScheduled.add (event);
        assert this.jobsExecuting.isEmpty ();
        this.jobsExecuting.add (job);
        for (SimEventAction<SimJob> action: this.startActions)
        {
          action.action (new SimEvent (time, job, action));
        }
        final SimEventAction sAction = job.getQueueStartAction ();
        if (sAction != null)
        {
          sAction.action (new SimEvent (time, job, sAction));
        }
      }
    }

    public FIFO (final SimEventQueue eventQueue)
    {
      super (eventQueue);
    }

  }

  public static class LIFO extends FIFO
  {

    public LIFO (final SimEventQueue eventQueue)
    {
      super (eventQueue);
    }

  }

  public static class RANDOM extends FIFO
  {

    protected final Random RNG;

    public RANDOM (final SimEventQueue eventQueue)
    {
      this (eventQueue, null);
    }

    public RANDOM (final SimEventQueue eventQueue, final Random RNG)
    {
      super (eventQueue);
      this.RNG = ((RNG == null) ? new Random () : RNG);
    }

  }

  public static class IS extends NonPreemptiveQueue
  {

    @Override
    public void arrive (final SimJob job, final double time)
    {
      // System.out.println ("Arrival at IS-queue @" + time);
      assert job.getQueue () == null;
      assert ! this.jobQueue.contains (job);
      assert time >= this.lastEventTime;
      this.lastEventTime = time;
      this.jobQueue.add (job);
      job.setQueue (this);
      final SimEvent<SimJob> event = new NonPreemptiveQueue.DepartureEvent
        (time + job.getServiceTime (this), job);
      this.eventQueue.add (event);
      this.eventsScheduled.add (event);
      this.jobsExecuting.add (job);
      for (SimEventAction<SimJob> action: this.arrivalActions)
      {
        action.action (new SimEvent (time, job, action));
      }
      final SimEventAction<SimJob> aAction = job.getQueueArriveAction ();
      if (aAction != null)
      {
        aAction.action (new SimEvent (time, job, aAction));
      }
      for (SimEventAction<SimJob> action: this.startActions)
      {
        action.action (new SimEvent (time, job, action));
      }
      final SimEventAction<SimJob> sAction = job.getQueueStartAction ();
      if (sAction != null)
      {
        sAction.action (new SimEvent (time, job, sAction));
      }
   }

    @Override
    public boolean revoke
      (final SimJob job,
      final double time,
      final boolean interruptService)
    {
      assert job.getQueue () == this;
      assert time >= this.lastEventTime;
      this.lastEventTime = time;
      if (! interruptService)
      {
        return false;
      }
      else
      {
        boolean found = this.jobsExecuting.remove (job);
        assert found;
        SimEvent<SimJob> event = null;
        final Iterator<SimEvent<SimJob>> i
          = this.eventsScheduled.iterator ();
        while (i.hasNext ())
        {
          final SimEvent<SimJob> e = i.next ();
          if (e.getObject () == job)
          {
            event = e;
            break;
          }
        }
        found = this.eventsScheduled.remove (event);
        assert found;
        found = this.eventQueue.remove (event);
        assert found;
      }
      job.setQueue (null);
      final boolean found = this.jobQueue.remove (job);
      assert found;
      final SimEventAction<SimJob> rAction = job.getQueueRevokeAction ();
      if (rAction !=  null)
      {
        rAction.action (new SimEvent<SimJob> (time, job, rAction));
      }
      return true;
    }

    @Override
    protected void rescheduleAfterDeparture
      (final SimJob departedJob, final double time)
    {
    }

    public IS (final SimEventQueue eventQueue)
    {
      super (eventQueue);
    }

  }

}
