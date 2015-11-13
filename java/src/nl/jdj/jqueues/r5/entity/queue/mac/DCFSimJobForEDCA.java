package nl.jdj.jqueues.r5.entity.queue.mac;

/**
 *
 */
public class DCFSimJobForEDCA
extends DCFSimJob
{

  private final EDCASimJob edcaSimJob;
  
  public final EDCASimJob getEDCASimJob ()
  {
    return this.edcaSimJob;
  }
  
  public DCFSimJobForEDCA (final EDCASimJob edcaSimJob)
  {
    super ();
    if (edcaSimJob == null)
      throw new IllegalArgumentException ();
    this.edcaSimJob = edcaSimJob;
  }
  
}