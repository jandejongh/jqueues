package nl.jdj.jqueues.r4.util.loadfactory.pattern;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.util.jobfactory.DefaultVisitsLoggingSimJob;
import nl.jdj.jqueues.r4.util.loadfactory.LoadFactory_SQ_SV;

/** An enumeration of known (and already instantiated) concrete {@link LoadFactory_SQ_SV}s is this package.
 * 
 * <p>
 * Intended for automated testing, allowing to iterate over all known (test) load factories.
 *
 */
public enum KnownLoadFactory_SQ_SV
{

  KLF_001 (new LoadFactory_SQ_SV_001 ()),
  KLF_002 (new LoadFactory_SQ_SV_002 ()),
  KLF_003 (new LoadFactory_SQ_SV_003 ()),
  KLF_004 (new LoadFactory_SQ_SV_004 ()),
  KLF_005 (new LoadFactory_SQ_SV_005 ()),
    ;
  
  /** Creates the entry in this enumeration.
   * 
   * @param loadFactory The instantiated load factory.
   * 
   * @throws IllegalArgumentException If the load factory is {@code null}.
   * 
   */
  KnownLoadFactory_SQ_SV (final LoadFactory_SQ_SV loadFactory)
  {
    if (loadFactory == null)
      throw new IllegalArgumentException ();
    this.loadFactory = loadFactory;
  }
  
  private final LoadFactory_SQ_SV loadFactory;
  
  /** Gets the (fixed) load factory corresponding to this {@link KnownLoadFactory_SQ_SQ} member.
   * 
   * @return The (fixed) load factory.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   *
   */
  public final <J extends DefaultVisitsLoggingSimJob, Q extends SimQueue> LoadFactory_SQ_SV<J, Q> getLoadFactory ()
  {
    return this.loadFactory;
  }
  
}
