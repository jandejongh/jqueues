package nl.jdj.jqueues.r5.util.predictor.state;

import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.util.predictor.workload.DefaultWorkloadSchedule;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleHandler;

/** A handler for an extension to a {@link DefaultSimQueueState}.
 * 
 * <p>
 * For a more detailed description of the rationale and architecture of extensions to a {@link DefaultSimQueueState},
 * see the description of {@link WorkloadScheduleHandler} for {@link DefaultWorkloadSchedule}.
 *
 */
public interface SimQueueStateHandler
{

  /** Returns the name of the handler.
   * 
   * <p>
   * The handler name must be unique within the realm of the {@link DefaultSimQueueState} at which this
   * handler registers. For {@link SimQueue} state extensions, the convention is to use
   * the interface name appended with "Handler", like, "SimQueueHandler" and "SimQueueWithGateHandler".
   * 
   * @return The name of the handler (must remain fixed during the handler's lifetime).
   * 
   */
  public String getHandlerName ();

  /** Initializes the handler, and passes the {@link DefaultSimQueueState} object.
   * 
   * <p>
   * This method is called only once during registration at the {@link DefaultSimQueueState} object.
   * 
   * @param queueState The {@link DefaultSimQueueState} at which we register, non-{@code null}.
   * 
   * @see DefaultSimQueueState#registerHandler
   * 
   */
  public void initHandler (DefaultSimQueueState queueState);
  
  /** Resets the (state represented by) this handler.
   * 
   * @param queueState The {@link DefaultSimQueueState} at which we are registered, non-{@code null}.
   * 
   * @see SimQueueState#reset
   * @see DefaultSimQueueState#reset
   * 
   */
  public void resetHandler (DefaultSimQueueState queueState);
  
}
