package nl.jdj.jqueues.r5.util.predictor.workload;

/** A checked {@link Exception} used in {@link WorkloadSchedule} and related classes and interfaces.
 *
 * <p>
 * Thrown to indicate that a {@link WorkloadSchedule} is ambiguous in cases were that is not allowed.
 * 
 * <p>This implementation merely mimics the constructors from its superclass.
 * The constructors are therefore undocumented; see {@link Exception} for their semantics.
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
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
