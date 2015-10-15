package nl.jdj.jqueues.r4.swing;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/**
 *
 */
public enum KnownSimQueue
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // VALUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  // serverless
  DROP    ("DROP",  false, nl.jdj.jqueues.r4.serverless.DROP.class,  GeneratorProfile.SE),
  SINK    ("SINK",  false, nl.jdj.jqueues.r4.serverless.SINK.class,  GeneratorProfile.SE),
  DELAY   ("DELAY", false, nl.jdj.jqueues.r4.serverless.DELAY.class, GeneratorProfile.SE_WST),
  ZERO    ("ZERO",  false, nl.jdj.jqueues.r4.serverless.ZERO.class,  GeneratorProfile.SE),
  
  // nonpreemptive
  NO_BUFFER_C ("NoBuffer_c", false, nl.jdj.jqueues.r4.nonpreemptive.NoBuffer_c.class, GeneratorProfile.UNKNOWN),
  FCFS        ("FCFS",       false, nl.jdj.jqueues.r4.nonpreemptive.FCFS.class,       GeneratorProfile.SE),
  FCFS_FB     ("FCFS_FB",    false, nl.jdj.jqueues.r4.nonpreemptive.FCFS_FB.class,    GeneratorProfile.UNKNOWN),
  FCFS_c      ("FCFS_c",     false, nl.jdj.jqueues.r4.nonpreemptive.FCFS_c.class,     GeneratorProfile.UNKNOWN),
  LCFS        ("LCFS",       false, nl.jdj.jqueues.r4.nonpreemptive.LCFS.class,       GeneratorProfile.SE),
  RANDOM      ("RANDOM",     false, nl.jdj.jqueues.r4.nonpreemptive.RANDOM.class,     GeneratorProfile.SE),
  SJF         ("SJF",        false, nl.jdj.jqueues.r4.nonpreemptive.SJF.class,        GeneratorProfile.SE),
  LJF         ("LJF",        false, nl.jdj.jqueues.r4.nonpreemptive.LJF.class,        GeneratorProfile.SE),
  IS          ("IS",         false, nl.jdj.jqueues.r4.nonpreemptive.IS.class,         GeneratorProfile.SE),
  IS_CST      ("IS_CST",     false, nl.jdj.jqueues.r4.nonpreemptive.IS_CST.class,     GeneratorProfile.SE_WST),
  IC          ("IC",         false, nl.jdj.jqueues.r4.nonpreemptive.IC.class,         GeneratorProfile.SE),
  
  // composite
  ENCAPSULATOR   ("Encapsulator",  true, nl.jdj.jqueues.r4.composite.BlackEncapsulatorSimQueue.class,
                                         GeneratorProfile.UNKNOWN),
  TANDEM         ("Tandem",        true, nl.jdj.jqueues.r4.composite.BlackTandemSimQueue.class,
                                         GeneratorProfile.UNKNOWN),
  COMP_TANDEM_2  ("Comp_Tandem_2", true, nl.jdj.jqueues.r4.composite.BlackCompressedTandem2SimQueue.class,
                                         GeneratorProfile.UNKNOWN),
  PARALLEL       ("Parallel",      true, nl.jdj.jqueues.r4.composite.BlackParallelSimQueues.class,
                                         GeneratorProfile.UNKNOWN),
  FB_PROB        ("FB_Prob",       true, nl.jdj.jqueues.r4.composite.BlackProbabilisticFeedbackSimQueue.class,
                                         GeneratorProfile.UNKNOWN),
  FB_VISITS      ("FB_NumVisits",  true, nl.jdj.jqueues.r4.composite.BlackNumVisitsFeedbackSimQueue.class,
                                         GeneratorProfile.UNKNOWN),
  JACKSON        ("Jackson",       true, nl.jdj.jqueues.r4.composite.BlackJacksonSimQueueNetwork.class,
                                         GeneratorProfile.UNKNOWN),
  
  // unknown
  UNKNOWN ("Unknown", true,  null, GeneratorProfile.UNKNOWN);

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Class<? extends SimQueue> queueClass;
  
  public final Class<? extends SimQueue> getQueueClass ()
  {
    return this.queueClass;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // COMPOSITE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final boolean composite;
  
  public final boolean isComposite ()
  {
    return this.composite;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final String defaultName;
  
  public final String getDefaultName ()
  {
    return this.defaultName;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GENERATOR PROFILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final GeneratorProfile generatorProfile;
  
  private final GeneratorProfile getGeneratorProfile ()
  {
    return this.generatorProfile;
  }
  
  public enum GeneratorProfile
  {
    
    SE       (true,  true,  false),
    SE_WST   (true,  true,  true),
    UNKNOWN  (false, false, false);
    
    private final boolean canInstantiate;
    
    private final boolean requiresSimEventList;
    
    private final boolean requiresWaitServiceTime;
    
    private GeneratorProfile
      (final boolean canInstatiate,
       final boolean requiresSimEventList,
       final boolean requiresWaitServiceTime)
    {
      this.canInstantiate = canInstatiate;
      this.requiresSimEventList = requiresSimEventList;
      this.requiresWaitServiceTime = requiresWaitServiceTime;
    }
    
    private SimQueue newInstance (final Class<? extends SimQueue> queueClass, final Parameters parameters)
    {
      if (! this.canInstantiate)
      {
        System.err.println ("Cannot instantiate profile " + this + ".");
        return null;
      }
      if (queueClass == null)
      {
        System.err.println ("Cannot instantiate null SimQueue class instance with profile " + this + ".");
        return null;
      }
      if (parameters == null)
      {
        System.err.println ("No parameters for new SimQueue instance with profile " + this + ".");
        return null;
      }
      if (this.requiresSimEventList && parameters.eventList == null)
      {
        System.err.println ("No event-list supplied for new SimQueue instance with profile " + this + ".");
        return null;
      }
      try
      {
        if (this == SE)
        {
          final Constructor constructor = queueClass.getConstructor (SimEventList.class);
          return (SimQueue) constructor.newInstance (parameters.eventList);
        }
        else if (this == SE_WST)
        {
          final Constructor constructor = queueClass.getConstructor (SimEventList.class, Double.TYPE);
          return (SimQueue) constructor.newInstance (parameters.eventList, parameters.waitServiceTime);
        }
        else
        {
          System.err.println ("Unsupported instantiation of new SimQueue instance with profile " + this + ".");
          return null;
        }
      }
      catch (NoSuchMethodException nsme)
      {
        System.err.println ("Could not find suitable constructor for new SimQueue instance with profile " + this + ".");
        return null;
      }
      catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
      {
        System.err.println ("Could not find instantiate new SimQueue instance with profile " + this + ": " + e + ".");
        return null;
      }
    }

  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private KnownSimQueue
  (final String defaultName,
    final boolean composite,
    final Class<? extends SimQueue> queueClass,
    final GeneratorProfile generatorProfile)
  {
    this.queueClass = queueClass;
    this.defaultName = defaultName;
    this.composite = composite;
    this.generatorProfile = generatorProfile;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // newInstance
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public final SimQueue newInstance (final Parameters parameters)
  {
    final GeneratorProfile generatorProfile = getGeneratorProfile ();
    if (generatorProfile == null)
    {
      System.err.println ("No generator profile on " + this + ".");
      return null;
    }
    final SimQueue queue = generatorProfile.newInstance (getQueueClass (), parameters);
    if (queue != null)
    {
      if (parameters.queueAccessVacation)
        queue.startQueueAccessVacation ();
      else
        queue.stopQueueAccessVacation ();
      queue.setServerAccessCredits (parameters.serverAccessCredits);
    }
    else
      System.err.println ("Generator profile " + generatorProfile + " on " + this + " failed to generate a new SimQueue.");
    return queue;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // valueOf FOR GIVEN SimQueue
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public static KnownSimQueue valueOf (final SimQueue queue)
  {
    if (queue == null)
      return null;
    for (KnownSimQueue ksq : KnownSimQueue.values ())
      if (queue.getClass () == ksq.getQueueClass ())
        return ksq;
    System.err.println ("Unknown SimQueue type of " + queue +".");
    return UNKNOWN;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PARAMETERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public final static class Parameters
  {
    
    public SimEventList eventList = null;
    
    public Set<SimQueue> queues = null;
    
    public boolean queueAccessVacation = false;
    
    public int serverAccessCredits = Integer.MAX_VALUE;
    
    public double waitServiceTime = 0;
    
  }
  
}
