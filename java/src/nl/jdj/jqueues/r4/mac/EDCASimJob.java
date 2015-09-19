package nl.jdj.jqueues.r4.mac;

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
