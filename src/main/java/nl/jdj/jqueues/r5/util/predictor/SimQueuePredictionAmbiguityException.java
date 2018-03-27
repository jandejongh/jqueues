package nl.jdj.jqueues.r5.util.predictor;

/** Thrown to indicate that a {@link SimQueuePredictor} or related object cannot produce a unique prediction.
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
public class SimQueuePredictionAmbiguityException
extends SimQueuePredictionException
{
  
  public SimQueuePredictionAmbiguityException ()
  {
    super ();
  }
  
  public SimQueuePredictionAmbiguityException (final String message)
  {
    super (message);
  }
  
  public SimQueuePredictionAmbiguityException (final String message, final Throwable cause)
  {
    super (message, cause);
  }
  
  public SimQueuePredictionAmbiguityException (final Throwable cause)
  {
    super (cause);
  }
  
}
