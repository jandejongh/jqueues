package nl.jdj.jqueues.r5.util.predictor;

/** Thrown to indicate that a {@link SimQueuePredictor} or related object cannot produce one or more predictions
 *  because a certain complexity threshold (e.g., in terms of number of predictions) is exceeded.
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
public class SimQueuePredictionComplexityException
extends SimQueuePredictionException
{
  
  public SimQueuePredictionComplexityException ()
  {
    super ();
  }
  
  public SimQueuePredictionComplexityException (final String message)
  {
    super (message);
  }
  
  public SimQueuePredictionComplexityException (final String message, final Throwable cause)
  {
    super (message, cause);
  }
  
  public SimQueuePredictionComplexityException (final Throwable cause)
  {
    super (cause);
  }
  
}
