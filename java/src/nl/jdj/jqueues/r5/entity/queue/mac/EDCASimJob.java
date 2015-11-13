package nl.jdj.jqueues.r5.entity.queue.mac;

/**
 *
 */
public class EDCASimJob
extends DCFSimJob
{

  public final ACParameters acParameters;
  
  public EDCASimJob (ACParameters acParameters)
  {
    if (acParameters == null)
      throw new IllegalArgumentException ();
    this.acParameters = acParameters;
  }
  
}
