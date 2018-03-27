package nl.jdj.jqueues.r5.util.predictor;

/** A checked {@link Exception} used in {@link SimQueuePredictor} and related classes and interfaces.
 *
 * <p>
 * Thrown to indicate that a {@link SimQueuePredictor} cannot fulfill its task.
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
public abstract class SimQueuePredictionException
extends Exception
{
  
  public SimQueuePredictionException ()
  {
    super ();
  }
  
  public SimQueuePredictionException (final String message)
  {
    super (message);
  }
  
  public SimQueuePredictionException (final String message, final Throwable cause)
  {
    super (message, cause);
  }
  
  public SimQueuePredictionException (final Throwable cause)
  {
    super (cause);
  }
  
}
