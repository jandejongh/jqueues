package nl.jdj.jqueues.r5.util.predictor;

/** Thrown to indicate that a {@link SimQueuePredictor} or related object cannot produce a prediction because its input is invalid.
 * 
 * <p>This implementation merely mimics the constructors from its superclass.
 * The constructors are therefore undocumented; see {@link Exception} for their semantics.
 * 
 */
public class SimQueuePredictionInvalidInputException
extends SimQueuePredictionException
{
  
  public SimQueuePredictionInvalidInputException ()
  {
    super ();
  }
  
  public SimQueuePredictionInvalidInputException (final String message)
  {
    super (message);
  }
  
  public SimQueuePredictionInvalidInputException (final String message, final Throwable cause)
  {
    super (message, cause);
  }
  
  public SimQueuePredictionInvalidInputException (final Throwable cause)
  {
    super (cause);
  }
  
}
