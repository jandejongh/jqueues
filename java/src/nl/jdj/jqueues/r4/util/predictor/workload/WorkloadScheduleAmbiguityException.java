package nl.jdj.jqueues.r4.util.predictor.workload;

/** A checked {@link Exception} used in {@link WorkloadSchedule} and related classes and interfaces.
 *
 * <p>
 * Thrown to indicate that a {@link WorkloadSchedule} is ambiguous in cases were that is not allowed.
 * 
 * <p>This implementation merely mimics the constructors from its superclass.
 * The constructors are therefore undocumented; see {@link Exception} for their semantics.
 * 
 */
public class WorkloadScheduleAmbiguityException
extends WorkloadScheduleException
{
  
  public WorkloadScheduleAmbiguityException ()
  {
    super ();
  }
  
  public WorkloadScheduleAmbiguityException (final String message)
  {
    super (message);
  }
  
  public WorkloadScheduleAmbiguityException (final String message, final Throwable cause)
  {
    super (message, cause);
  }
  
  public WorkloadScheduleAmbiguityException (final Throwable cause)
  {
    super (cause);
  }
  
}
