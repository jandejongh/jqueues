package nl.jdj.jqueues.r5.util.loadfactory.pattern;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.visitslogging.DefaultVisitsLoggingSimJob;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;

/** An enumeration of known (and already instantiated) concrete {@link LoadFactory_SQ_SV}s is this package.
 * 
 * <p>
 * Intended for automated testing, allowing to iterate over all known (test) load factories.
 *
 */
public enum KnownLoadFactory_SQ_SV
{

  KLF_0001 (new LoadFactory_SQ_SV_0001 ()),
  KLF_0002 (new LoadFactory_SQ_SV_0002 ()),
  KLF_0005 (new LoadFactory_SQ_SV_0005 ()),
  KLF_0010 (new LoadFactory_SQ_SV_0010 ()),
  KLF_0011 (new LoadFactory_SQ_SV_0011 ()),
  KLF_0012 (new LoadFactory_SQ_SV_0012 ()),
  KLF_0013 (new LoadFactory_SQ_SV_0013 ()),
  KLF_0014 (new LoadFactory_SQ_SV_0014 ()),
  KLF_0100 (new LoadFactory_SQ_SV_0100 ()),
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
  
  /** Gets the (fixed) load factory corresponding to this {@link KnownLoadFactory_SQ_SV} member.
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
