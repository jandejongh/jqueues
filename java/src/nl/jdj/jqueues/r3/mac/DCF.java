package nl.jdj.jqueues.r3.mac;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.DefaultSimQueueListener;
import nl.jdj.jqueues.r4.NonPreemptiveQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r3.composite.BlackTandemSimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/**
 *
 */
public class DCF
extends BlackTandemSimQueue<AbstractSimJob, NonPreemptiveQueue.FCFS, DCFSimJob, DCF>
implements MediumPhyStateObserver
{

  /** The slot time in seconds.
   * 
   */
  private final double slotTime_s;
  
  /** The contention window (in slots).
   * 
   */
  private final int cw;
  
  /** The AIFS (in slots).
   * 
   */
  private final int aifs_slots;
  
  /** The DIFS (in micro-seconds).
   * 
   */
  private final double difs_mus;
  
  /** The EIFS (in micro-seconds).
   * 
   */
  private final double eifs_mus;
  
  private final NonPreemptiveQueue.FCFS waitQueue;
  
  private final NonPreemptiveQueue.FCFS contentionQueue;
  
  private final MediumPhyStateMonitor mediumPhyStateMonitor;
  
  private static Set<NonPreemptiveQueue.FCFS> createQueues (final SimEventList eventList)
  {
    final Set<NonPreemptiveQueue.FCFS> set = new LinkedHashSet<>  ();
    final NonPreemptiveQueue.FCFS waitQueue = new NonPreemptiveQueue.FCFS (eventList);
    waitQueue.setServerAccessCredits (1);
    set.add (waitQueue);
    final NonPreemptiveQueue.FCFS contentionQueue = new NonPreemptiveQueue.FCFS (eventList);
    contentionQueue.setServerAccessCredits (0);
    set.add (contentionQueue);
    return set;
  }

  public DCF
  (SimEventList eventList,
    MediumPhyStateMonitor mediumPhyStateMonitor,
    final double slotTime_s,
    final int aifs_slots,
    final double difs_mus,
    final double eifs_mus,
    final int cw)
  {
    super (eventList, createQueues (eventList), null);
    if (getQueues ().size () != 2)
      throw new IllegalStateException ();
    final Iterator<NonPreemptiveQueue.FCFS> iterator = getQueues ().iterator ();
    this.waitQueue = iterator.next ();
    this.contentionQueue = iterator.next ();
    this.contentionQueue.registerQueueListener (new DefaultSimQueueListener<AbstractSimJob, SimQueue> ()
    {
      @Override
      public void arrival (double t, AbstractSimJob job, SimQueue queue)
      {
        DCF.this.contentionQueueArrival (t);
      }
    });
    this.registerQueueListener (new DefaultSimQueueListener<DCFSimJob, DCF> ()
    {
      @Override
      public void departure (double t, DCFSimJob job, DCF queue)
      {
        DCF.this.uponQueueDeparture (t, job);
      }      
    });
    if (mediumPhyStateMonitor == null)
      throw new IllegalArgumentException ();
    this.mediumPhyStateMonitor = mediumPhyStateMonitor;
    if (slotTime_s <= 0 || aifs_slots < 0 || difs_mus < 0 || eifs_mus < 0 || cw < 0)
      throw new IllegalArgumentException ();
    this.slotTime_s = slotTime_s;
    this.aifs_slots = aifs_slots;
    this.difs_mus = difs_mus;
    this.eifs_mus = eifs_mus;
    this.cw = cw;
    this.mediumPhyStateMonitor.registerMediumPhyStateObserver (this);
    setState (DCFState.IDLE_NAV, getEventList ().getTime ());
  }
  
  public DCF
  (SimEventList eventList,
    MediumPhyStateMonitor mediumPhyStateMonitor,
    final double slotTime_s,
    ACParameters acParameters,
    final double difs_mus,
    final double eifs_mus)
  {
    this (eventList, mediumPhyStateMonitor, slotTime_s, acParameters.aifs_slots, difs_mus, eifs_mus, acParameters.cw);
  }
  
  /** The slot time.
   *
   * @return The slot time, in seconds.
   *
   */
  protected final double slotTime_s ()
  {
    return this.slotTime_s;
  }

  /** The random number generator used for contention, i.e.,
   *    drawing initial values of the back-off counter.
   *
   */
  private final Random RNG_CONTENTION = new Random ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MediumPhyStateObserver
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private MediumPhyState lastMediumPhyState = MediumPhyState.IDLE;
  
  @Override
  public void mediumPhyStateUpdate (final double time, final MediumPhyState mediumPhyState)
  {
    if (mediumPhyState == null)
      throw new IllegalArgumentException ();
    if (time < getLastUpdateTime ())
      throw new IllegalArgumentException ();
    if (mediumPhyState != this.lastMediumPhyState)
    {
      update (time);
      final boolean wasTx = (this.lastMediumPhyState == MediumPhyState.TX_BUSY);
      this.lastMediumPhyState = mediumPhyState;
      if (wasTx)
        txOwnTransmissionEnds (time);
      else
        switch (this.lastMediumPhyState)
        {
          case RX_BUSY:
            rxMediumGoesBusy (time);
            break;
          case TX_BUSY:
            txMediumGoesBusy (time);
            break;
          case IDLE:
            rxMediumGoesIdle (time);
            break;
          default:
            throw new RuntimeException ();
        }
    }
  }

  /** Indication whether last reception was successful.
   *
   */
  private boolean lastRxSuccess = true;

  @Override
  public void lastReceptionUpdate (double time, boolean success)
  {
    this.lastRxSuccess = success;
  }
  
  /** Can a transmission take place, given the current level of interference
   * (i.e., thermal noise and ongoing transmissions)?
   *
   * This is the fundamental carrier-sense function.
   * 
   * @return True if the medium is idle in the physical sense.
   *
   */
  protected final boolean canTransmit ()
  {
    return this.lastMediumPhyState == MediumPhyState.IDLE;
  }

  
  //////////////////////////////////////////////////////////////////////////////
  //                                                                          //
  // DCF STATE LISTENERS.                                                     //
  //                                                                          //
  //////////////////////////////////////////////////////////////////////////////

  private final Set<DCFStateListener> listeners = new LinkedHashSet<> ();
  
  public final void registerDCFStateListener (DCFStateListener listener)
  {
    if (listener != null)
      this.listeners.add (listener);
  }
  
  public final void unregisterDCFStateListener (DCFStateListener listener)
  {
    this.listeners.remove (listener);
  }

  private void fireNewDCFState (final double time, final DCFState oldState, final DCFState newState)
  {
    for (DCFStateListener l : this.listeners)
      l.newDCFState (time, this, oldState, newState);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  //                                                                          //
  // STATE ADMINISTRATION AND STATE TRANSITIONS.                              //
  //                                                                          //
  //////////////////////////////////////////////////////////////////////////////


  /** Current state.
   *
   * Set more properly in constructor.
   *
   */
  private DCFState state = DCFState.IDLE_INIT;

  /** Get the current state.
   *
   * @return The current state.
   *
   */
  public final DCFState getState ()
  {
    return this.state;
  }

  /** Return true if the MAC is in one of the idle states
   *    (i.e., has no jobs in its queue and is not transmitting).
   *
   * The terminology may be a bit misleading here.
   * The MAC is considered 'idle'
   * when it is not contending for the medium in any way.
   * It has nothing to do with the state of the {@link Medium},
   * i.e., whether or not the medium is sensed 'idle';
   * see {@link #isMediumMACIdle()} for that.
   * Note that even if the MAC is idle, it may still undergo state changes,
   * e.g., due to IFS and/or NAV expiration.
   * The MAC is idle only if it is in one of the following states:
   * {@link State#IDLE_SHARP},
   * {@link State#IDLE_IFS},
   * {@link State#IDLE_BACKOFF},
   * {@link State#IDLE_RX}, or
   * {@link State#IDLE_NAV}.
   *
   * @return True if the MAC is idle (i.e., has nothing to transmit).
   *
   * @see #isMediumMACIdle()
   * @see State
   *
   */
  public final boolean isMACIdle ()
  {
    switch (state)
    {
      case IDLE_SHARP:
      case IDLE_IFS:
      case IDLE_BACKOFF:
      case IDLE_RX:
      case IDLE_NAV:
      {
        return true;
      }
      case BUSY_IFS:
      case BUSY_BACKOFF:
      case BUSY_RX:
      case BUSY_NAV:
      case BUSY_TX:
      {
        return false;
      }
      default:
      {
        throw new RuntimeException ("Illegal state.");
      }
    }
  }

  /** Return true if the underlying {@link Medium} is idle,
   *    both in the physical and the virtual (NAV) sense.
   *
   * <p> This function considers the medium idle if it is idle in the
   * physical sense (see {@link #isMediumIdle()} and
   * available for transmission in the MAC sense,
   * i.e., considering IFS, NAV and back-off.
   *
   * <p> The state of the medium is derived from the current MAC {@link State}.
   * The {@link Medium} is idle only if it is in one of the following states:
   * {@link State#IDLE_SHARP},
   * {@link State#IDLE_BACKOFF}, or
   * {@link State#BUSY_BACKOFF}.
   * Note that while awaiting IFS expiration, i.e., in states
   * {@link State#IDLE_IFS} and
   * {@link State#BUSY_IFS},
   * we consider the medium busy,
   * as it is unavailable for transmissions.
   *
   * <p> During back-offs, i.e., in states
   * {@link State#IDLE_BACKOFF} and
   * {@link State#BUSY_BACKOFF},
   * the medium is considered idle,
   * since we are contending for the medium
   * (either because we have a frame to transmit,
   * or because we want to reach the {@link State#IDLE_SHARP} state).
   *
   * @return True if the medium is idle (in physical and virtual sense).
   *
   * @see #isMACIdle()
   * @see #isMediumIdle()
   * @see State
   *
   */
  public final boolean isMediumMACIdle ()
  {
    switch (state)
    {
      case IDLE_SHARP:
      case IDLE_BACKOFF:
      case BUSY_BACKOFF:
      {
        return true;
      }
      case IDLE_IFS:
      case IDLE_RX:
      case IDLE_NAV:
      case BUSY_IFS:
      case BUSY_RX:
      case BUSY_NAV:
      case BUSY_TX:
      {
        return false;
      }
      default:
      {
        throw new RuntimeException ("Illegal state.");
      }
    }
  }

  /** Return true if the underlying {@link Medium} is idle,
   *    in the physical sense.
   *
   * In essence, the medium is considered idle if no
   * transmission is (physically) being sensed on it.
   * The state of the medium is derived from the current MAC {@link State}.
   * The {@link Medium} is idle only if it is not transmitting and not receiving,
   * i.e., if is is not in
   * {@link State#IDLE_RX},
   * {@link State#BUSY_RX}, or
   * {@link State#BUSY_TRANSMIT}.
   *
   * @return True if the medium is idle in physical sense.
   *
   * @see #isMACIdle()
   * @see #isMediumMACIdle()
   * @see State
   *
   */
  public final boolean isMediumIdle ()
  {
    switch (state)
    {
      case IDLE_IFS:
      case IDLE_NAV:
      case IDLE_SHARP:
      case IDLE_BACKOFF:
      case BUSY_IFS:
      case BUSY_NAV:
      case BUSY_BACKOFF:
      {
        return true;
      }
      case IDLE_RX:
      case BUSY_RX:
      case BUSY_TX:
      {
        return false;
      }
      default:
      {
        throw new RuntimeException ("Illegal state.");
      }
    }
  }

  /** Central state-changing method.
   *
   * This is the only place where we manipulate the DCF state.
   * If applicable, the {@link #mediumMACStateChangedListener} is notified
   * of channel-state changes, but only after all state administration
   * and event scheduling have been updated.
   *
   * @param state The new state.
   * @param time  Time at which the state is entered (i.e., the current time).
   *
   * @see #checkStateTransition(State)
   * @see State
   * @see #mediumMACStateChangedListener
   *
   */
  protected void setState (final DCFState state, final double time)
  {
    checkStateTransition (state);
    //
    // STATE-EXIT ACTIONS.
    //
    if ((this.state == DCFState.IDLE_IFS && state != DCFState.BUSY_IFS)
      || this.state == DCFState.BUSY_IFS)
    {
      // Revoke IFS timer if still scheduled.
      revokeIFSTimer ();
    }
    if ((this.state == DCFState.IDLE_NAV && state != DCFState.BUSY_NAV)
      || this.state == DCFState.BUSY_NAV)
    {
      // Revoke NAV timer if still scheduled.
      revokeNAVTimer ();
    }
    if ((this.state == DCFState.IDLE_BACKOFF && state != DCFState.BUSY_BACKOFF)
      || this.state == DCFState.BUSY_BACKOFF)
    {
      // Revoke backoff timer if still scheduled.
      revokeSlotTimer ();
    }
    if ((this.state == DCFState.IDLE_RX && state != DCFState.BUSY_RX)
      || this.state == DCFState.BUSY_RX)
    {
      // Frame received; update NAV.
      // XXX Where to get the received transmission?
      // updateNAV (null, time);
    }
    if (this.state == DCFState.BUSY_TX)
    {
      // XXX Nothing to do?
      // this.transmitting = null;
    }
    //
    // STATE CHANGE NOTIFICATIONS.
    //
    fireNewDCFState (time, this.state, state);
    //
    // STATE-ENTRY ACTIONS.
    //
    if ((this.state == DCFState.IDLE_SHARP && state != DCFState.BUSY_TX)
      || (this.state == DCFState.BUSY_TX))
    {
      redrawBC (time);
    }
    if ((state == DCFState.BUSY_IFS && this.state != DCFState.IDLE_IFS)
      || state == DCFState.IDLE_IFS)
    {
      // Schedule IFS timer.
      rescheduleIFSTimer (time);
    }
    if ((state == DCFState.BUSY_NAV && this.state != DCFState.IDLE_NAV)
      || state == DCFState.IDLE_NAV)
    {
      rescheduleNAVTimer (time);
    }
    if ((state == DCFState.BUSY_BACKOFF && this.state != DCFState.IDLE_BACKOFF)
      || state == DCFState.IDLE_BACKOFF)
    {
      // Schedule backoff timer.
      rescheduleSlotTimer (time, slotTime_s ());
    }
    if (state == DCFState.BUSY_TX)
    {
      // Initiate transmission.
      // transmit (time);
      this.contentionQueue.setServerAccessCredits (1);
      this.waitQueue.setServerAccessCredits (1);
    }
    this.state = state;
  }

  /** Check a state transition.
   *
   * Checks the transition of the current state ({@link #getState}) to
   *   caller-supplied new state.
   *
   * @param state The new state.
   *
   * @throws RuntimeException If the state transition is illegal.
   *
   * @see #setState(State,double)
   * @see State
   *
   */
  protected void checkStateTransition (final DCFState state)
  {
    switch (state)
    {
      case IDLE_SHARP:
      {
        switch (this.state)
        {
          case IDLE_BACKOFF:
          {
            return;
          }
        }
        break;
      }
      case IDLE_IFS:
      {
        switch (this.state)
        {
          case IDLE_NAV:
          {
            return;
          }
        }
        break;
      }
      case IDLE_BACKOFF:
      {
        switch (this.state)
        {
          case IDLE_IFS:
          {
            return;
          }
        }
        break;
      }
      case IDLE_RX:
      {
        switch (this.state)
        {
          case IDLE_SHARP:
          case IDLE_IFS:
          case IDLE_BACKOFF:
          case IDLE_NAV:
          case BUSY_TX:
          {
            return;
          }
        }
        break;
      }
      case IDLE_NAV:
      {
        switch (this.state)
        {
          case IDLE_INIT:
          case IDLE_RX:
          case BUSY_RX:
          case BUSY_TX:
          {
            return;
          }
        }
        break;
      }
      case BUSY_IFS:
      {
        switch (this.state)
        {
          case IDLE_IFS:
          case BUSY_NAV:
          {
            return;
          }
        }
        break;
      }
      case BUSY_BACKOFF:
      {
        switch (this.state)
        {
          case IDLE_BACKOFF:
          case BUSY_IFS:
          {
            return;
          }
        }
        break;
      }
      case BUSY_RX:
      {
        switch (this.state)
        {
          case IDLE_RX:
          case BUSY_IFS:
          case BUSY_BACKOFF:
          case BUSY_NAV:
          case BUSY_TX:
          {
            return;
          }
        }
        break;
      }
      case BUSY_NAV:
      {
        switch (this.state)
        {
          case IDLE_NAV:
          case BUSY_RX:
          case BUSY_TX:
          {
            return;
          }
        }
        break;
      }
      case BUSY_TX:
      {
        switch (this.state)
        {
          case IDLE_SHARP:
          case BUSY_BACKOFF:
          {
            return;
          }
        }
        break;
      }
    }
    throw new RuntimeException ("Illegal state (transition).");
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //                                                                          //
  // EXHAUSTIVE LISTING OF (POTENTIAL) STATE-CHANGING EVENTS.                 //
  //                                                                          //
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  protected void ifsExpired (final double time)
  {
    switch (this.state)
    {
      case IDLE_IFS:
      {
        setState (DCFState.IDLE_BACKOFF, time);
        break;
      }
      case BUSY_IFS:
      {
        setState (DCFState.BUSY_BACKOFF, time);
        break;
      }
      default:
      {
        throw new RuntimeException ("Illegal state and/or event.");
      }
    }
  }

  protected void navExpired (final double time)
  {
    switch (this.state)
    {
      case IDLE_NAV:
      {
        setState (DCFState.IDLE_IFS, time);
        break;
      }
      case BUSY_NAV:
      {
        setState (DCFState.BUSY_IFS, time);
        break;
      }
      default:
      {
        throw new RuntimeException ("Illegal state and/or event.");
      }
    }
  }

  protected void slotExpired (final double time)
  {
    switch (this.state)
    {
      case IDLE_BACKOFF:
      case BUSY_BACKOFF:
      {
        if (this.bc == 0)
        {
          backoffExpired (time);
        }
        else
        {
          this.bc--;
          rescheduleSlotTimer (time, slotTime_s ());
        }
        break;
      }
      default:
      {
        throw new RuntimeException ("Illegal state and/or event.");
      }
    }
  }

  protected void backoffExpired (final double time)
  {
    switch (this.state)
    {
      case IDLE_BACKOFF:
      {
        setState (DCFState.IDLE_SHARP, time);
        break;
      }
      case BUSY_BACKOFF:
      {
        setState (DCFState.BUSY_TX, time);
        break;
      }
      default:
      {
        throw new RuntimeException ("Illegal state and/or event.");
      }
    }
  }

  protected void rxMediumGoesIdle (final double time)
  {
    switch (this.state)
    {
      case IDLE_RX:
      {
        setState (DCFState.IDLE_NAV, time);
        break;
      }
      case BUSY_RX:
      {
        setState (DCFState.BUSY_NAV, time);
        break;
      }
      default:
      {
        throw new RuntimeException ("Illegal state and/or event.");
      }
    }
  }

  protected void rxMediumGoesBusy (final double time)
  {
    switch (this.state)
    {
      case IDLE_SHARP:
      case IDLE_IFS:
      case IDLE_BACKOFF:
      case IDLE_NAV:
      {
        setState (DCFState.IDLE_RX, time);
        break;
      }
      case BUSY_IFS:
      case BUSY_BACKOFF:
      case BUSY_NAV:
      {
        setState (DCFState.BUSY_RX, time);
        break;
      }
      default:
      {
        throw new RuntimeException ("Illegal state and/or event.");
      }
    }
  }

  protected void txOwnTransmissionEnds (final double time)
  {
    switch (this.state)
    {
      case BUSY_TX:
      {
        if (getNumberOfJobs () == 0)
        {
          if (canTransmit ())
          {
            setState (DCFState.IDLE_NAV, time);
          }
          else
          {
            setState (DCFState.IDLE_RX, time);
          }
        }
        else
        {
          // At this point, we start contending for the next frame in the queue.
          // XXX this.WAIT_QUEUE.get (0).startContentionTime = time;
          if (canTransmit ())
          {
            setState (DCFState.BUSY_NAV, time);
          }
          else
          {
            setState (DCFState.BUSY_RX, time);
          }
        }
        break;
      }
      default:
      {
        throw new RuntimeException ("Illegal state and/or event.");
      }
    }
  }
  
  protected void txMediumGoesBusy (final double time)
  {
    // NOTHING TO DO!
  }

  protected void contentionQueueArrival (final double time)
  {
    // NOTE: WE ARE CALLED FROM A NOTIFICATION!
    // SCHEDULE STATE_CHANGE ON EVENT LIST!
    getEventList ().add (new SimEvent (time, null, new SimEventAction ()
    {

      @Override
      public void action (SimEvent event)
      {
        switch (DCF.this.state)
        {
          case IDLE_SHARP:
          {
            setState (DCFState.BUSY_TX, time);
            break;
          }
          case IDLE_IFS:
          {
            setState (DCFState.BUSY_IFS, time);
            break;
          }
          case IDLE_BACKOFF:
         {
            setState (DCFState.BUSY_BACKOFF, time);
            break;
          }
          case IDLE_RX:
          {
            setState (DCFState.BUSY_RX, time);
            break;
          }
          case IDLE_NAV:
          {
            setState (DCFState.BUSY_NAV, time);
            break;
          }
          case BUSY_IFS:
          case BUSY_BACKOFF:
          case BUSY_RX:
          case BUSY_NAV:
          case BUSY_TX:
          {
            break;
          }
          default:
          {
            throw new RuntimeException ("Illegal state and/or event.");
          }
        }
      }
    }));
  }
  
  protected void uponQueueDeparture (final double time, final DCFSimJob job)
  {
    // NOTE: WE ARE CALLED FROM A NOTIFICATION!
    // SCHEDULE STATE_CHANGE ON EVENT LIST!
    getEventList ().add (new SimEvent (time, null, new SimEventAction ()
    {

      @Override
      public void action (SimEvent event)
      {
        DCF.this.mediumPhyStateMonitor.startTransmission (time, DCF.this, job);
      }
    }));
  }
  
  
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //                                                                          //
  // NAV.                                                                     //
  //                                                                          //
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////


  private double navExpireTime = -1;

  public final boolean idleNAV (final double time)
  {
    return time >= navExpireTime;
  }

  public final double navExpireTime ()
  {
    return this.navExpireTime;
  }

  //void updateNAV (final TransmissionSimJob t, final double time)
  //{
  //  if (t != null)
  //  {
  //    // this.navExpireTime = ...
  //  }
  //}

  protected final SimEventAction navExpiredAction
    = new SimEventAction ()
  {
    @Override
    public void action (final SimEvent event)
    {
      DCF.this.navExpired (event.getTime ());
    }
  };

  protected final SimEvent navExpiredEvent
    = new SimEvent (0, this, this.navExpiredAction);

  protected final void rescheduleNAVTimer (final double time)
  {
    if (this.navExpireTime < time)
      this.navExpireTime = time;
    this.navExpiredEvent.setTime (this.navExpireTime);
    getEventList ().add (this.navExpiredEvent);
  }

  protected final void revokeNAVTimer ()
  {
    if (getEventList ().contains (this.navExpiredEvent))
      getEventList ().remove (this.navExpiredEvent);
  }


  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //                                                                          //
  // IFS (DIFS/EIFS + AIFS[AC]).                                              //
  //                                                                          //
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////


  /** The IFS to use (including AIFS), in microseconds.
   *
   * @return The IFS.
   *
   */
  private double ifs_mus ()
  {
      final double d_e_ifs_mus = (this.lastRxSuccess ? this.difs_mus : this.eifs_mus);
      final double aifs_mus = this.aifs_slots * slotTime_s * 1.0E6;
      final double ifs_mus = d_e_ifs_mus + aifs_mus;
      return ifs_mus;
  }

  /** The IFS to use (including AIFS), in seconds.
   *
   * Either DIFS or EIFS depending on whether the last reception was successful,
   * increased by the AC-specific AIFS.
   *
   * @return The IFS.
   *
   */
  public final double ifs_s ()
  {
    return ifs_mus () * 1.0E-6;
  }

  protected final SimEventAction IFS_EXPIRED_ACTION
    = new SimEventAction ()
  {
    @Override
    public void action (final SimEvent event)
    {
      DCF.this.ifsExpired (event.getTime ());
    }
  };

  protected final SimEvent IFS_EXPIRED_EVENT
    = new SimEvent (0, this, this.IFS_EXPIRED_ACTION);

  void rescheduleIFSTimer (final double time)
  {
    revokeIFSTimer ();
    IFS_EXPIRED_EVENT.setTime (time + ifs_s ());
    getEventList ().add (IFS_EXPIRED_EVENT);
  }

  void revokeIFSTimer ()
  {
    if (getEventList ().contains (this.IFS_EXPIRED_EVENT))
      getEventList ().remove (this.IFS_EXPIRED_EVENT);
  }

  
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //                                                                          //
  // BACKOFF (CW/CWMIN/CWMAX/BC).                                             //
  //                                                                          //
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////


  /** The back-off counter.
   *
   */
  private int bc;

  /** Redraw the back-off counter.
   *
   * Back-off counter drawn from U({0, 1, ..., cw}).
   * Source: IEEE Std 802.11-2007.
   *
   * @param time The current time (ignored).
   * 
   */
  protected final void redrawBC (final double time)
  {
    // The '+1' below is required to include this.cw as possible outcome,
    // see Random.nextInt (int) for details.
    this.bc = this.RNG_CONTENTION.nextInt (this.cw + 1);
  }

  /** The action corresponding to {@link #SLOT_EXPIRED_EVENT}.
   *
   * The action is called as a result of an internally-scheduled
   * slot-expiration event, {@link #SLOT_EXPIRED_EVENT}.
   *
   */
  protected final SimEventAction SLOT_EXPIRED_ACTION
    = new SimEventAction ()
  {
    @Override
    public void action (final SimEvent event)
    {
      DCF.this.slotExpired (event.getTime ());
    }
  };

  /** The event for slot expiration.
   *
   * Since the MAC can only wait for a single expiration event, it is simply
   * made an object member and continuously reused (i.e., rescheduled).
   *
   * @see #SLOT_EXPIRED_ACTION
   *
   */
  protected final SimEvent SLOT_EXPIRED_EVENT
    = new SimEvent<> (0, this, this.SLOT_EXPIRED_ACTION);

  protected void rescheduleSlotTimer (final double time, final double slotTime_s)
  {
    revokeSlotTimer ();
    SLOT_EXPIRED_EVENT.setTime (time + slotTime_s);
    getEventList ().add (this.SLOT_EXPIRED_EVENT);
  }

  protected final void revokeSlotTimer ()
  {
    if (getEventList ().contains (this.SLOT_EXPIRED_EVENT))
      getEventList ().remove (this.SLOT_EXPIRED_EVENT);
  }

}
