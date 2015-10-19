package nl.jdj.jqueues.r4.mac;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r4.composite.BlackParallelSimQueues;
import nl.jdj.jqueues.r4.composite.SimQueueSelector;
import nl.jdj.jsimulation.r4.SimEventList;

/**
 *
 */
public class EDCA
extends BlackParallelSimQueues<DCFSimJob, DCF, EDCASimJob, EDCA>
implements SimQueueSelector<EDCASimJob, DCFSimJob, DCF>
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
  
  private final Map<ACParameters, DCF> acMap = new HashMap<> ();
  
  private final MediumPhyStateMonitor mediumPhyStateMonitor;

  private static Set<DCF> createQueues (final SimEventList eventList,
    final MediumPhyStateMonitor mediumPhyStateMonitor,
    final Set<? extends ACParameters> acParameters,
    final double slotTime_s, final double difs_mus, final double eifs_mus)
  {
    if (acParameters == null || acParameters.contains (null) || acParameters.isEmpty ())
      throw new IllegalArgumentException ();
    final Set<DCF> set = new LinkedHashSet<>  ();
    for (ACParameters acp : acParameters)
    {
      final DCF dcf = new DCF (eventList, mediumPhyStateMonitor, slotTime_s, acp, difs_mus, eifs_mus);
      set.add (dcf);
    }
    return set;
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
    final Set<? extends ACParameters> acps)
  {
    super (eventList,
      createQueues (eventList, mediumPhyStateMonitor, acps, slotTime_s, difs_mus, eifs_mus),
      createDelegateSimJobFactory (),
      null);
    if (getQueues ().size () != acps.size ())
      throw new RuntimeException ();
    final Iterator<DCF> queueIterator = getQueues ().iterator ();
    for (ACParameters acp : acps)
      this.acMap.put (acp, queueIterator.next ());
    this.slotTime_s = slotTime_s;
    this.difs_mus = difs_mus;
    this.eifs_mus = eifs_mus;
    this.mediumPhyStateMonitor = mediumPhyStateMonitor;
  }

  @Override
  public EDCA getCopySimQueue ()
  {
    return new EDCA
      (getEventList (), this.mediumPhyStateMonitor, this.slotTime_s, this.difs_mus, this.eifs_mus, this.acMap.keySet ());
  }
  
  @Override
  public DCF selectFirstQueue (double time, EDCASimJob job)
  {
    if (job == null || job.acParameters == null || ! this.acMap.containsKey (job.acParameters))
      throw new IllegalArgumentException ();
    final DCF firstQueue = this.acMap.get (job.acParameters);
    if (firstQueue == null || ! getQueues ().contains (firstQueue))
      throw new IllegalArgumentException ();
    return firstQueue;
  }

  @Override
  public DCF selectNextQueue (double time, EDCASimJob job, DCF previousQueue)
  {
    if (job == null || job.acParameters == null || (! this.acMap.containsKey (job.acParameters))
      || previousQueue == null || ! getQueues ().contains (previousQueue))
      throw new IllegalArgumentException ();
    return null;
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

}
