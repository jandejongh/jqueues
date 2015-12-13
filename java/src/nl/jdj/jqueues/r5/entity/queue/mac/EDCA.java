package nl.jdj.jqueues.r5.entity.queue.mac;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;
import nl.jdj.jqueues.r5.entity.queue.composite.parallel.AbstractBlackParallelSimQueues;
import nl.jdj.jqueues.r5.extensions.qos.SimQueueQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/**
 *
 */
public class EDCA<P extends Enum<P> & AC>
extends AbstractBlackParallelSimQueues<DCFSimJob, DCF, EDCASimJob, EDCA>
implements SimQueueQoS<EDCASimJob, EDCA, P>
{

  /** The slot time in seconds.
   * 
   */
  private final double slotTime_s;
  
  /** The DIFS (in micro-seconds).
   * 
   */
  private final double difs_mus;
  
  /** The EIFS (in micro-seconds).
   * 
   */
  private final double eifs_mus;
  
  private final Map<P, DCF> acMap;
  
  public final Map<P, DCF> getACMap ()
  {
    return this.acMap;
  }
  
  private final MediumPhyStateMonitor mediumPhyStateMonitor;

  private static <P extends Enum<P> & AC> Set<DCF> createQueues
  (final SimEventList eventList,
    final MediumPhyStateMonitor mediumPhyStateMonitor,
    final Class<P> qosClass,
    final double slotTime_s,
    final double difs_mus,
    final double eifs_mus)
  {
    if (qosClass == null)
      throw new IllegalArgumentException ();
    final Set<DCF> set = new LinkedHashSet<>  ();
    for (final P ac : qosClass.getEnumConstants ())
    {
      final DCF dcf = new DCF (eventList, mediumPhyStateMonitor, slotTime_s, ac.getACParameters (), difs_mus, eifs_mus);
      set.add (dcf);
    }
    return set;
  }

  private static <P extends Enum<P> & AC> SimQueueSelector<EDCASimJob, DCF> createSimQueueSelector (final Class<P> qosClass)
  {
    
      return new SimQueueSelector<EDCASimJob, DCF> ()
      {

        @Override
        public DCF selectFirstQueue (double time, EDCASimJob job)
        {
          if (job == null)
            throw new IllegalArgumentException ();
          if (job.getQoSClass () != qosClass)
            throw new IllegalArgumentException ();
          final EDCA<P> queue = (EDCA<P>) job.getQueue ();
          final P qos = (P) queue.getAndCheckJobQoS (job);
          if (! queue.getACMap ().containsKey (qos))
            throw new IllegalArgumentException ();
          final DCF firstQueue = queue.getACMap ().get (qos);
          if (firstQueue == null || ! queue.getQueues ().contains (firstQueue))
            throw new IllegalArgumentException ();
          return firstQueue;
        }

        @Override
        public DCF selectNextQueue (double time, EDCASimJob job, DCF previousQueue)
        {
          return null;
        }

      };
      
    }
      
  private static DCFSimJobForEDCAFactory createDelegateSimJobFactory ()
  {
    return new DCFSimJobForEDCAFactory ();
  }
  
  public EDCA
  (final SimEventList eventList,
    final MediumPhyStateMonitor mediumPhyStateMonitor,
    final double slotTime_s,
    final double difs_mus,
    final double eifs_mus,
    final Class<P> qosClass,
    final P defaultJobQoS)
  {
    super (eventList,
      createQueues (eventList, mediumPhyStateMonitor, qosClass, slotTime_s, difs_mus, eifs_mus),
      createSimQueueSelector (qosClass),
      createDelegateSimJobFactory ());
    if (qosClass == null || defaultJobQoS == null)
      throw new IllegalArgumentException ();
    this.qosClass = qosClass;
    this.defaultJobQoS = defaultJobQoS;
    if (getQueues ().size () != this.qosClass.getEnumConstants ().length)
      throw new RuntimeException ();
    this.acMap = new EnumMap<> (this.qosClass);
    final Iterator<DCF> queueIterator = getQueues ().iterator ();
    for (final P ac : this.qosClass.getEnumConstants ())
      this.acMap.put (ac, queueIterator.next ());
    this.slotTime_s = slotTime_s;
    this.difs_mus = difs_mus;
    this.eifs_mus = eifs_mus;
    this.mediumPhyStateMonitor = mediumPhyStateMonitor;
  }

  @Override
  public EDCA getCopySimQueue ()
  {
    return new EDCA
      (getEventList (),
        this.mediumPhyStateMonitor,
        this.slotTime_s,
        this.difs_mus,
        this.eifs_mus,
        this.qosClass,
        this.defaultJobQoS);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Class<P> qosClass;

  @Override
  public final Class<? extends P> getQoSClass ()
  {
    return this.qosClass;
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void setQoSClass (final Class<? extends P> qosClass)
  {
    SimQueueQoS.super.setQoSClass (qosClass);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns {@code null}, since the QoS value of a queue has no meaning.
   * 
   * @return {@code null}.
   * 
   */
  @Override
  public final P getQoS ()
  {
    return null;
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void setQoS (final P qos)
  {
    SimQueueQoS.super.setQoS (qos);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (DEFAULT) JOB QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final P defaultJobQoS;

  @Override
  public final P getDefaultJobQoS ()
  {
    return this.defaultJobQoS;
  }

  /** Gets the (validated) QoS value for given job (which does not have to be present in the queue yet).
   * 
   * <p>
   * The QoS value is validated in the sense that if the {@link SimJob} returns a non-{@code null}
   * {@link SimJob#getQoSClass}, the class or interface returned must be a sub-class or sub-interface
   * of {@link #getQoSClass}, in other words,
   * the job's QoS structure must be compatible with this queue.
   * In addition, if the job return non-{@code null} {@link SimJob#getQoSClass},
   * it must return a non-{@code null} QoS value from {@link SimJob#getQoS},
   * and this QoS value must be an instance of the reported job QoS class.
   * In all other case, including the case in which the job is {@code null},
   * an {@link IllegalArgumentException} is thrown.
   * 
   * @param job The job, non-{@code null}.
   * 
   * @return The validated QoS value of the job, taking the default (only) if the job reports {@code null} QoS class and value.
   * 
   * @throws IllegalArgumentException If the job is {@code null} or if one or more QoS-related sanity checks fail.
   * 
   */
  protected final P getAndCheckJobQoS (final EDCASimJob<P> job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (job.getQoSClass () == null)
    {
      if (job.getQoS () != null)
        throw new IllegalArgumentException ();
      else
        return getDefaultJobQoS ();
    }
    else
    {
      if (! getQoSClass ().isAssignableFrom (job.getQoSClass ()))
        throw new IllegalArgumentException ();
      if (job.getQoS () == null)
        return getDefaultJobQoS ();
      if (! getQoSClass ().isInstance (job.getQoS ()))
        throw new IllegalArgumentException ();
      return job.getQoS ();
    }
  }

  /** Returns "EDCA".
   * 
   * @return "EDCA".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "EDCA";
  }

  private final NavigableMap<P, Set<EDCASimJob>> jobsQoSMap = new TreeMap<> ();
  
  @Override
  public final NavigableMap<P, Set<EDCASimJob>> getJobsQoSMap ()
  {
    this.jobsQoSMap.clear ();
    for (final P ac : this.acMap.keySet ())
    {
      final DCF dcf = this.acMap.get (ac);
      if (! dcf.getJobs ().isEmpty ())
      {
        this.jobsQoSMap.put (ac, new LinkedHashSet<> ());
        for (final DCFSimJob dcfSimJob : dcf.getJobs ())
          this.jobsQoSMap.get (ac).add (getRealJob (dcfSimJob, dcf));
      }
    }
    return this.jobsQoSMap;
  }

}
