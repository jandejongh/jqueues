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
  
  public DefaultSimQueuePrediction_SQ_SV
  (final Q queue,
   final Map<SimJob, JobQueueVisitLog<SimJob, Q>> visitLogs,
   final List<Map<Double, Boolean>> qavLog)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    if (visitLogs == null)
      throw new IllegalArgumentException ();
    if (qavLog == null)
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
    this.queue = queue;
    this.visitLogs = visitLogs;
    this.qavLog = qavLog;
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
  
}
