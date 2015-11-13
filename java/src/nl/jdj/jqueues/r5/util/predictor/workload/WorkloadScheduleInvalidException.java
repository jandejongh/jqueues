package nl.jdj.jqueues.r5.util.predictor.workload;

/** A checked {@link Exception} used in {@link WorkloadSchedule} and related classes and interfaces.
 *
 * <p>
 * Thrown to indicate that a {@link WorkloadSchedule} is invalid.
 * 
 * <p>This implementation merely mimics the constructors from its superclass.
 * The constructors are therefore undocumented; see {@link Exception} for their semantics.
 * 
 */
public class WorkloadScheduleInvalidException
extends WorkloadScheduleException
{
  
  public WorkloadScheduleInvalidException ()
  {
    super ();
  }
  
  public WorkloadScheduleInvalidException (final String message)
  {
    super (message);
  }
  
  public WorkloadScheduleInvalidException (final String message, final Throwable cause)
  {
    super (message, cause);
  }
  
  public WorkloadScheduleInvalidException (final Throwable cause)
  {
    super (cause);
  }
  
}
