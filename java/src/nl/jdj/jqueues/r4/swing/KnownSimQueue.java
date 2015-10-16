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
  DROP    ("DROP",  false, nl.jdj.jqueues.r4.serverless.DROP.class,  GeneratorProfile.SE,
           NumberOfServersProfile.NOS_ALWAYS_ZERO),
  SINK    ("SINK",  false, nl.jdj.jqueues.r4.serverless.SINK.class,  GeneratorProfile.SE,
           NumberOfServersProfile.NOS_ALWAYS_ZERO),
  DELAY   ("DELAY", false, nl.jdj.jqueues.r4.serverless.DELAY.class, GeneratorProfile.SE_WST,
           NumberOfServersProfile.NOS_ALWAYS_ZERO),
  ZERO    ("ZERO",  false, nl.jdj.jqueues.r4.serverless.ZERO.class,  GeneratorProfile.SE,
           NumberOfServersProfile.NOS_ALWAYS_ZERO),
  
  // nonpreemptive
  NO_BUFFER_c ("NoBuffer_c", false, nl.jdj.jqueues.r4.nonpreemptive.NoBuffer_c.class, GeneratorProfile.SE_c,
               NumberOfServersProfile.NOS_FINITE),
  FCFS        ("FCFS",       false, nl.jdj.jqueues.r4.nonpreemptive.FCFS.class,       GeneratorProfile.SE,
               NumberOfServersProfile.NOS_ALWAYS_ONE),
  FCFS_B      ("FCFS_B",     false, nl.jdj.jqueues.r4.nonpreemptive.FCFS_B.class,     GeneratorProfile.SE_B,
               NumberOfServersProfile.NOS_ALWAYS_ONE),
  FCFS_c      ("FCFS_c",     false, nl.jdj.jqueues.r4.nonpreemptive.FCFS_c.class,     GeneratorProfile.SE_c,
               NumberOfServersProfile.NOS_FINITE),
  LCFS        ("LCFS",       false, nl.jdj.jqueues.r4.nonpreemptive.LCFS.class,       GeneratorProfile.SE,
               NumberOfServersProfile.NOS_ALWAYS_ONE),
  RANDOM      ("RANDOM",     false, nl.jdj.jqueues.r4.nonpreemptive.RANDOM.class,     GeneratorProfile.SE,
               NumberOfServersProfile.NOS_ALWAYS_ONE),
  SJF         ("SJF",        false, nl.jdj.jqueues.r4.nonpreemptive.SJF.class,        GeneratorProfile.SE,
               NumberOfServersProfile.NOS_ALWAYS_ONE),
  LJF         ("LJF",        false, nl.jdj.jqueues.r4.nonpreemptive.LJF.class,        GeneratorProfile.SE,
               NumberOfServersProfile.NOS_ALWAYS_ONE),
  IS          ("IS",         false, nl.jdj.jqueues.r4.nonpreemptive.IS.class,         GeneratorProfile.SE,
               NumberOfServersProfile.NOS_ALWAYS_INFINITE),
  IS_CST      ("IS_CST",     false, nl.jdj.jqueues.r4.nonpreemptive.IS_CST.class,     GeneratorProfile.SE_WST,
               NumberOfServersProfile.NOS_ALWAYS_INFINITE),
  IC          ("IC",         false, nl.jdj.jqueues.r4.nonpreemptive.IC.class,         GeneratorProfile.SE,
               NumberOfServersProfile.NOS_ALWAYS_INFINITE),
  
  // composite
  ENCAPSULATOR   ("Encapsulator",  true, nl.jdj.jqueues.r4.composite.BlackEncapsulatorSimQueue.class,
                  GeneratorProfile.UNKNOWN, NumberOfServersProfile.NOS_IRRELEVANT),
  DROP_COLLECTOR ("DropCollector", true, nl.jdj.jqueues.r4.composite.BlackDropCollectorSimQueue.class,
                  GeneratorProfile.UNKNOWN, NumberOfServersProfile.NOS_IRRELEVANT),
  TANDEM         ("Tandem",        true, nl.jdj.jqueues.r4.composite.BlackTandemSimQueue.class,
                  GeneratorProfile.UNKNOWN, NumberOfServersProfile.NOS_IRRELEVANT),
  COMP_TANDEM_2  ("Comp_Tandem_2", true, nl.jdj.jqueues.r4.composite.BlackCompressedTandem2SimQueue.class,
                  GeneratorProfile.UNKNOWN, NumberOfServersProfile.NOS_IRRELEVANT),
  PARALLEL       ("Parallel",      true, nl.jdj.jqueues.r4.composite.BlackParallelSimQueues.class,
                  GeneratorProfile.UNKNOWN, NumberOfServersProfile.NOS_IRRELEVANT),
  JSQ            ("JSQ",           true, nl.jdj.jqueues.r4.composite.BlackJoinShortestSimQueue.class,
                  GeneratorProfile.UNKNOWN, NumberOfServersProfile.NOS_IRRELEVANT),
  FB_PROB        ("FB_Prob",       true, nl.jdj.jqueues.r4.composite.BlackProbabilisticFeedbackSimQueue.class,
                  GeneratorProfile.UNKNOWN, NumberOfServersProfile.NOS_IRRELEVANT),
  FB_VISITS      ("FB_NumVisits",  true, nl.jdj.jqueues.r4.composite.BlackNumVisitsFeedbackSimQueue.class,
                  GeneratorProfile.UNKNOWN, NumberOfServersProfile.NOS_IRRELEVANT),
  JACKSON        ("Jackson",       true, nl.jdj.jqueues.r4.composite.BlackJacksonSimQueueNetwork.class,
                  GeneratorProfile.UNKNOWN, NumberOfServersProfile.NOS_IRRELEVANT),
  
  // unknown
  UNKNOWN ("Unknown", true,  null, GeneratorProfile.UNKNOWN, NumberOfServersProfile.NOS_IRRELEVANT);

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
    
    SE       (true,  true,  false, false, false),
    SE_WST   (true,  true,  true,  false, false),
    SE_c     (true,  true,  false, true,  false),
    SE_B     (true,  true,  false, false, true),
    UNKNOWN  (false, false, false, false, false);
    
    private final boolean canInstantiate;
    
    private final boolean requiresSimEventList;
    
    private final boolean requiresWaitServiceTime;
    
    private final boolean requiresNumberOfServers;
    
    private final boolean requiresBufferSize;
    
    private GeneratorProfile
      (final boolean canInstatiate,
       final boolean requiresSimEventList,
       final boolean requiresWaitServiceTime,
       final boolean requiresNumberOfServers,
       final boolean requiresBufferSize)
    {
      this.canInstantiate = canInstatiate;
      this.requiresSimEventList = requiresSimEventList;
      this.requiresWaitServiceTime = requiresWaitServiceTime;
      this.requiresNumberOfServers = requiresNumberOfServers;
      this.requiresBufferSize = requiresBufferSize;
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
        else if (this == SE_c)
        {
          final Constructor constructor = queueClass.getConstructor (SimEventList.class, Integer.TYPE);
          return (SimQueue) constructor.newInstance (parameters.eventList, parameters.numberOfServers);
        }
        else if (this == SE_B)
        {
          final Constructor constructor = queueClass.getConstructor (SimEventList.class, Integer.TYPE);
          return (SimQueue) constructor.newInstance (parameters.eventList, parameters.bufferSize);
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
  // NUMBER OF SERVERS PROFILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final NumberOfServersProfile numberOfServersProfile;
  
  public final NumberOfServersProfile getNumberOfServersProfile ()
  {
    return this.numberOfServersProfile;
  }
  
  public enum NumberOfServersProfile
  {
    
    NOS_IRRELEVANT      ( 0,  0,  0),
    NOS_ALWAYS_ZERO     ( 0,  0,  0),
    NOS_ALWAYS_ONE      ( 1,  1,  1),
    NOS_ALWAYS_INFINITE (-1, -1, -1),
    NOS_NON_ZERO        ( 1, -1, -1),
    NOS_FINITE          ( 0, Integer.MAX_VALUE, 1),
    NOS_NON_ZERO_FINITE ( 1, Integer.MAX_VALUE, 1),
    NOS_ANY             ( 0, -1, -1);

    private final int minVal;
    
    private final int maxVal;
    
    private final int defVal;
    
    private NumberOfServersProfile (final int minVal, final int maxVal, final int defVal)
    {
      this.minVal = minVal;
      this.maxVal = maxVal;
      this.defVal = defVal;
    }
    
    public final int getMinValue ()
    {
      return this.minVal;
    }
    
    public final int getMaxValue ()
    {
      return this.maxVal;
    }
    
    public final int getDefValue ()
    {
      return this.defVal;
    }
    
    public final boolean isUserSettable ()
    {
      return getMinValue () != getMaxValue ();
    }
    
    public final boolean isValidValue (int val)
    {
      if (val < -1)
        return false;
      if (! isUserSettable ())
        return (val == getMinValue ());
      if (this.minVal == -1)
        return (val == -1);
      if (val != -1 && val < this.minVal)
        return false;
      if (this.maxVal == -1)
        return true;
      return (val != -1 && val <= this.maxVal);
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
    final GeneratorProfile generatorProfile,
    final NumberOfServersProfile numberOfServersProfile)
  {
    this.queueClass = queueClass;
    this.defaultName = defaultName;
    this.composite = composite;
    this.generatorProfile = generatorProfile;
    this.numberOfServersProfile = numberOfServersProfile;
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
    
    public int numberOfServers = 1;
    
    public int bufferSize = 10;
    
  }
  
}
