package nl.jdj.jqueues.r5.util.predictor;

/** Thrown to indicate that a {@link SimQueuePredictor} or related object cannot produce a unique prediction.
 * 
 * <p>This implementation merely mimics the constructors from its superclass.
 * The constructors are therefore undocumented; see {@link Exception} for their semantics.
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
