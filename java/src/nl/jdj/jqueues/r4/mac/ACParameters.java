package nl.jdj.jqueues.r4.mac;

/**
 *
 */
public class ACParameters
{
  
  /** The contention window (in slots).
   * 
   */
  public final int cw;
  
  /** The AIFS (in slots).
   * 
   */
  public final int aifs_slots;
  
  public ACParameters (final int cw, final int aifs_slots)
  {
    this.cw = cw;
    this.aifs_slots = aifs_slots;
  }
  
}
