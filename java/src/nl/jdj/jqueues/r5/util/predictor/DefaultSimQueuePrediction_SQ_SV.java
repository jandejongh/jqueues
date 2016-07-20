package nl.jdj.jqueues.r5.util.predictor;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;

/** A default implementation of a {@link SimQueuePrediction_SQ_SV}.
 *
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class DefaultSimQueuePrediction_SQ_SV<Q extends SimQueue>
implements SimQueuePrediction_SQ_SV<Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new prediction.
   * 
   * @param queue     The queue to which the prediction applies, non-{@code null}.
   * @param visitLogs The job-visit logs, non-{@code null}.
   * @param qavLog    The queue-access-vacation logs, non-{@code null}.
   * @param nwaLog    The {@code NoWaitArmed} logs, non-{@code null}.
   * 
   * @throws IllegalArgumentException If any of the arguments is {@code null} or improperly structured.
   * 
   */
  public DefaultSimQueuePrediction_SQ_SV
  (final Q queue,
   final Map<SimJob, JobQueueVisitLog<SimJob, Q>> visitLogs,
   final List<Map<Double, Boolean>> qavLog,
   final List<Map<Double, Boolean>> nwaLog)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    if (visitLogs == null)
      throw new IllegalArgumentException ();
    if (qavLog == null)
      throw new IllegalArgumentException ();
    if (nwaLog == null)
      throw new IllegalArgumentException ();
    if (visitLogs.containsKey (null) || visitLogs.containsValue (null))
      throw new IllegalArgumentException ();
    for (final Entry<SimJob, JobQueueVisitLog<SimJob, Q>> entry : visitLogs.entrySet ())
    {
      final SimJob job = entry.getKey ();
      final JobQueueVisitLog jqvl = entry.getValue ();
      if (jqvl.queue != queue)
        throw new IllegalArgumentException ();
      if (jqvl.job != job)
        throw new IllegalArgumentException ();
    }
    for (final Map<Double, Boolean> entry : qavLog)
      if (entry == null || entry.size () != 1)
        throw new IllegalArgumentException ();
    for (final Map<Double, Boolean> entry : nwaLog)
      if (entry == null || entry.size () != 1)
        throw new IllegalArgumentException ();
    this.queue = queue;
    this.visitLogs = visitLogs;
    this.qavLog = qavLog;
    this.nwaLog = nwaLog;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Q queue;
  
  @Override
  public final Q getQueue ()
  {
    return this.queue;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // VISIT LOGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<SimJob, JobQueueVisitLog<SimJob, Q>> visitLogs;
  
  @Override
  public final Map<SimJob, JobQueueVisitLog<SimJob, Q>> getVisitLogs ()
  {
    return this.visitLogs;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QAV LOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final List<Map<Double, Boolean>> qavLog;
  
  @Override
  public final List<Map<Double, Boolean>> getQueueAccessVacationLog ()
  {
    return this.qavLog;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NWA LOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final List<Map<Double, Boolean>> nwaLog;
  
  @Override
  public final List<Map<Double, Boolean>> getNoWaitArmedLog ()
  {
    return this.nwaLog;
  }
  
}
