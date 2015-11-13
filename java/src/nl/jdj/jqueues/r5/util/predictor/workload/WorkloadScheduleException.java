package nl.jdj.jqueues.r5.util.predictor.workload;

/** A checked {@link Exception} used in {@link WorkloadSchedule} and related classes and interfaces.
 *
 * <p>
 * Thrown to indicate that a {@link WorkloadSchedule} is invalid, or an invalid operation on it is requested.
 * 
 * <p>This implementation merely mimics the constructors from its superclass.
 * The constructors are therefore undocumented; see {@link Exception} for their semantics.
 * 
 */
public abstract class WorkloadScheduleException
extends Exception
{
  
  public WorkloadScheduleException ()
  {
    super ();
  }
  
  public WorkloadScheduleException (final String message)
  {
    super (message);
  }
  
  public WorkloadScheduleException (final String message, final Throwable cause)
  {
    super (message, cause);
  }
  
  public WorkloadScheduleException (final Throwable cause)
  {
    super (cause);
  }
  
}
