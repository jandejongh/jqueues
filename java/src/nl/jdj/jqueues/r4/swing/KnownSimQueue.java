package nl.jdj.jqueues.r4.swing;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.composite.DelegateSimJobFactory;
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
           IntegerParameterProfile.IPP_ALWAYS_ZERO, IntegerParameterProfile.IPP_IRRELEVANT,
           DoubleParameterProfile.DPP_IRRELEVANT),
  SINK    ("SINK",  false, nl.jdj.jqueues.r4.serverless.SINK.class,  GeneratorProfile.SE,
           IntegerParameterProfile.IPP_ALWAYS_ZERO, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
           DoubleParameterProfile.DPP_IRRELEVANT),
  DELAY   ("DELAY", false, nl.jdj.jqueues.r4.serverless.DELAY.class, GeneratorProfile.SE_WST,
           IntegerParameterProfile.IPP_ALWAYS_ZERO, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
           DoubleParameterProfile.DPP_POSITIVE),
  ZERO    ("ZERO",  false, nl.jdj.jqueues.r4.serverless.ZERO.class,  GeneratorProfile.SE,
           IntegerParameterProfile.IPP_ALWAYS_ZERO, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
           DoubleParameterProfile.DPP_IRRELEVANT),
  
  // nonpreemptive
  NO_BUFFER_c ("NoBuffer_c", false, nl.jdj.jqueues.r4.nonpreemptive.NoBuffer_c.class, GeneratorProfile.SE_c,
               IntegerParameterProfile.IPP_FINITE, IntegerParameterProfile.IPP_ALWAYS_ZERO,
               DoubleParameterProfile.DPP_IRRELEVANT),
  FCFS        ("FCFS",       false, nl.jdj.jqueues.r4.nonpreemptive.FCFS.class,       GeneratorProfile.SE,
               IntegerParameterProfile.IPP_ALWAYS_ONE, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
               DoubleParameterProfile.DPP_IRRELEVANT),
  FCFS_B      ("FCFS_B",     false, nl.jdj.jqueues.r4.nonpreemptive.FCFS_B.class,     GeneratorProfile.SE_B,
               IntegerParameterProfile.IPP_ALWAYS_ONE, IntegerParameterProfile.IPP_FINITE,
               DoubleParameterProfile.DPP_IRRELEVANT),
  FCFS_c      ("FCFS_c",     false, nl.jdj.jqueues.r4.nonpreemptive.FCFS_c.class,     GeneratorProfile.SE_c,
               IntegerParameterProfile.IPP_FINITE, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
               DoubleParameterProfile.DPP_IRRELEVANT),
  LCFS        ("LCFS",       false, nl.jdj.jqueues.r4.nonpreemptive.LCFS.class,       GeneratorProfile.SE,
               IntegerParameterProfile.IPP_ALWAYS_ONE, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
               DoubleParameterProfile.DPP_IRRELEVANT),
  RANDOM      ("RANDOM",     false, nl.jdj.jqueues.r4.nonpreemptive.RANDOM.class,     GeneratorProfile.SE,
               IntegerParameterProfile.IPP_ALWAYS_ONE, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
               DoubleParameterProfile.DPP_IRRELEVANT),
  SJF         ("SJF",        false, nl.jdj.jqueues.r4.nonpreemptive.SJF.class,        GeneratorProfile.SE,
               IntegerParameterProfile.IPP_ALWAYS_ONE, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
               DoubleParameterProfile.DPP_IRRELEVANT),
  LJF         ("LJF",        false, nl.jdj.jqueues.r4.nonpreemptive.LJF.class,        GeneratorProfile.SE,
               IntegerParameterProfile.IPP_ALWAYS_ONE, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
               DoubleParameterProfile.DPP_IRRELEVANT),
  IS          ("IS",         false, nl.jdj.jqueues.r4.nonpreemptive.IS.class,         GeneratorProfile.SE,
               IntegerParameterProfile.IPP_ALWAYS_INFINITE, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
               DoubleParameterProfile.DPP_IRRELEVANT),
  IS_CST      ("IS_CST",     false, nl.jdj.jqueues.r4.nonpreemptive.IS_CST.class,     GeneratorProfile.SE_WST,
               IntegerParameterProfile.IPP_ALWAYS_INFINITE, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
               DoubleParameterProfile.DPP_POSITIVE),
  IC          ("IC",         false, nl.jdj.jqueues.r4.nonpreemptive.IC.class,         GeneratorProfile.SE,
               IntegerParameterProfile.IPP_ALWAYS_INFINITE, IntegerParameterProfile.IPP_ALWAYS_INFINITE,
               DoubleParameterProfile.DPP_IRRELEVANT),
  
  // composite
  ENCAPSULATOR   ("Encapsulator", true, nl.jdj.jqueues.r4.composite.BlackEncapsulatorSimQueue.class,
                  GeneratorProfile.SE_Q_DSJF,
                  IntegerParameterProfile.IPP_IRRELEVANT,
                  IntegerParameterProfile.IPP_ALWAYS_INFINITE,
                  DoubleParameterProfile.DPP_IRRELEVANT),
  DROP_COLLECTOR ("DropCollector", true, nl.jdj.jqueues.r4.composite.BlackDropCollectorSimQueue.class,
                  GeneratorProfile.SE_Q1_Q2_DSJF,
                  IntegerParameterProfile.IPP_IRRELEVANT,
                  IntegerParameterProfile.IPP_ALWAYS_INFINITE,
                  DoubleParameterProfile.DPP_IRRELEVANT),
  TANDEM         ("Tandem", true, nl.jdj.jqueues.r4.composite.BlackTandemSimQueue.class,
                  GeneratorProfile.SE_QSET_DSJF,
                  IntegerParameterProfile.IPP_IRRELEVANT,
                  IntegerParameterProfile.IPP_ALWAYS_INFINITE,
                  DoubleParameterProfile.DPP_IRRELEVANT),
  COMP_TANDEM_2  ("Comp_Tandem_2", true, nl.jdj.jqueues.r4.composite.BlackCompressedTandem2SimQueue.class,
                  GeneratorProfile.SE_Q1_Q2_DSJF,
                  IntegerParameterProfile.IPP_IRRELEVANT,
                  IntegerParameterProfile.IPP_ALWAYS_INFINITE,
                  DoubleParameterProfile.DPP_IRRELEVANT),
  PARALLEL       ("Parallel", true, nl.jdj.jqueues.r4.composite.BlackParallelSimQueues.class,
                  GeneratorProfile.UNKNOWN,
                  IntegerParameterProfile.IPP_IRRELEVANT,
                  IntegerParameterProfile.IPP_ALWAYS_INFINITE,
                  DoubleParameterProfile.DPP_IRRELEVANT),
  JSQ            ("JSQ", true, nl.jdj.jqueues.r4.composite.BlackJoinShortestSimQueue.class,
                  GeneratorProfile.UNKNOWN,
                  IntegerParameterProfile.IPP_IRRELEVANT,
                  IntegerParameterProfile.IPP_ALWAYS_INFINITE,
                  DoubleParameterProfile.DPP_IRRELEVANT),
  FB_PROB        ("FB_Prob", true, nl.jdj.jqueues.r4.composite.BlackProbabilisticFeedbackSimQueue.class,
                  GeneratorProfile.UNKNOWN,
                  IntegerParameterProfile.IPP_IRRELEVANT,
                  IntegerParameterProfile.IPP_ALWAYS_INFINITE,
                  DoubleParameterProfile.DPP_IRRELEVANT),
  FB_VISITS      ("FB_NumVisits", true, nl.jdj.jqueues.r4.composite.BlackNumVisitsFeedbackSimQueue.class,
                  GeneratorProfile.UNKNOWN,
                  IntegerParameterProfile.IPP_IRRELEVANT,
                  IntegerParameterProfile.IPP_ALWAYS_INFINITE,
                  DoubleParameterProfile.DPP_IRRELEVANT),
  JACKSON        ("Jackson", true, nl.jdj.jqueues.r4.composite.BlackJacksonSimQueueNetwork.class,
                  GeneratorProfile.UNKNOWN,
                  IntegerParameterProfile.IPP_IRRELEVANT,
                  IntegerParameterProfile.IPP_ALWAYS_INFINITE,
                  DoubleParameterProfile.DPP_IRRELEVANT),
  
  // unknown
  UNKNOWN ("Unknown", true,  null,
           GeneratorProfile.UNKNOWN,
           IntegerParameterProfile.IPP_IRRELEVANT,
           IntegerParameterProfile.IPP_IRRELEVANT,
           DoubleParameterProfile.DPP_IRRELEVANT);

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
    
    SE            (true,  true,  false, false, false, false, 0, 0),
    SE_WST        (true,  true,  true,  false, false, false, 0, 0),
    SE_c          (true,  true,  false, true,  false, false, 0, 0),
    SE_B          (true,  true,  false, false, true,  false, 0, 0),
    SE_Q_DSJF     (true,  true,  false, false, false, true,  1, 1),
    SE_Q1_Q2_DSJF (true,  true,  false, false, false, true,  2, 2),
    SE_QSET_DSJF  (true,  true,  false, false, false, true,  0, Integer.MAX_VALUE),
    UNKNOWN       (false, false, false, false, false, false, 0, 0);
    
    private final boolean canInstantiate;
    
    private final boolean requiresSimEventList;
    
    private final boolean requiresWaitServiceTime;
    
    private final boolean requiresNumberOfServers;
    
    private final boolean requiresBufferSize;
    
    private final boolean requiresSubQueues;
    
    private final int minSubQueues;
    
    private final int maxSubQueues;
    
    private GeneratorProfile
      (final boolean canInstatiate,
       final boolean requiresSimEventList,
       final boolean requiresWaitServiceTime,
       final boolean requiresNumberOfServers,
       final boolean requiresBufferSize,
       final boolean requiresSubQueues,
       final int     minSubQueues,
       final int     maxSubQueues)
    {
      this.canInstantiate = canInstatiate;
      this.requiresSimEventList = requiresSimEventList;
      this.requiresWaitServiceTime = requiresWaitServiceTime;
      this.requiresNumberOfServers = requiresNumberOfServers;
      this.requiresBufferSize = requiresBufferSize;
      this.requiresSubQueues = requiresSubQueues;
      this.minSubQueues = minSubQueues;
      this.maxSubQueues = maxSubQueues;
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
      if (this.requiresSubQueues)
      {
        if (parameters.queues == null)
        {
          System.err.println ("No sub-queues supplied for new SimQueue instance with profile " + this + ".");
          return null;
        }
        else if (parameters.queues.size () < this.minSubQueues)
        {
          System.err.println ("Error: Not enough sub-queues supplied for new SimQueue instance with profile " + this + ".");
          System.err.println ("-> Minimum:  " + this.minSubQueues + ".");
          System.err.println ("-> Supplied: " + parameters.queues.size () + ".");
          return null;
        }
        else if (parameters.queues.size () > this.maxSubQueues)
        {
          System.err.println ("Warning: Too many sub-queues supplied for new SimQueue instance with profile " + this + ".");
          System.err.println ("-> Maximum:  " + this.maxSubQueues + ".");
          System.err.println ("-> Supplied: " + parameters.queues.size () + ".");
        }
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
        else if (this == SE_Q_DSJF)
        {
          final Constructor constructor = queueClass.getConstructor
            (SimEventList.class, SimQueue.class, DelegateSimJobFactory.class);
          final Iterator<SimQueue> iterator = parameters.queues.iterator ();
          final SimQueue q = iterator.next ();
          return (SimQueue) constructor.newInstance (parameters.eventList, q, null);
        }
        else if (this == SE_Q1_Q2_DSJF)
        {
          final Constructor constructor = queueClass.getConstructor
            (SimEventList.class, SimQueue.class, SimQueue.class, DelegateSimJobFactory.class);
          final Iterator<SimQueue> iterator = parameters.queues.iterator ();
          final SimQueue q1 = iterator.next ();
          final SimQueue q2 = iterator.next ();
          return (SimQueue) constructor.newInstance (parameters.eventList, q1, q2, null);
        }
        else if (this == SE_QSET_DSJF)
        {
          final Constructor constructor = queueClass.getConstructor
            (SimEventList.class, Set.class, DelegateSimJobFactory.class);
          final Set<SimQueue> qSet = new LinkedHashSet<> (parameters.queues);
          return (SimQueue) constructor.newInstance (parameters.eventList, qSet, null);
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
  // INTEGER PARAMETER PROFILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public enum IntegerParameterProfile
  {
    
    IPP_IRRELEVANT      ( 0,  0,  0),
    IPP_ALWAYS_ZERO     ( 0,  0,  0),
    IPP_ALWAYS_ONE      ( 1,  1,  1),
    IPP_ALWAYS_INFINITE (-1, -1, -1),
    IPP_NON_ZERO        ( 1, -1, -1),
    IPP_FINITE          ( 0, Integer.MAX_VALUE, 1),
    IPP_NON_ZERO_FINITE ( 1, Integer.MAX_VALUE, 1),
    IPP_ANY             ( 0, -1, -1);

    private final int minVal;
    
    private final int maxVal;
    
    private final int defVal;
    
    private IntegerParameterProfile (final int minVal, final int maxVal, final int defVal)
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
    
    public final boolean isValidValue (final int val)
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
  // DOUBLE PARAMETER PROFILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public enum DoubleParameterProfile
  {
    
    DPP_IRRELEVANT    ( 0.0,  0.0,  0.0),
    DPP_ALWAYS_ZERO   ( 0.0,  0.0,  0.0),
    DPP_ALWAYS_ONE    ( 1.0,  1.0,  1.0),
    DPP_ZERO_ONE_INC  ( 0.0,  1.0,  0.5),
    DPP_POSITIVE      ( 0.0,  Double.POSITIVE_INFINITY, 1.0),
    DPP_ANY           ( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0);

    private final double minVal;
    
    private final double maxVal;
    
    private final double defVal;
    
    private DoubleParameterProfile (final double minVal, final double maxVal, final double defVal)
    {
      this.minVal = minVal;
      this.maxVal = maxVal;
      this.defVal = defVal;
    }
    
    public final double getMinValue ()
    {
      return this.minVal;
    }
    
    public final double getMaxValue ()
    {
      return this.maxVal;
    }
    
    public final double getDefValue ()
    {
      return this.defVal;
    }
    
    public final boolean isUserSettable ()
    {
      return getMinValue () != getMaxValue ();
    }
    
    public final boolean isValidValue (final double val)
    {
      if (! isUserSettable ())
        return (val == getMinValue ());
      if (val < this.minVal)
        return false;
      return (val <= this.maxVal);
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NUMBER OF SERVERS PROFILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final IntegerParameterProfile numberOfServersProfile;
  
  public final IntegerParameterProfile getNumberOfServersProfile ()
  {
    return this.numberOfServersProfile;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BUFFER SIZE PROFILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final IntegerParameterProfile bufferSizeProfile;
  
  public final IntegerParameterProfile getBufferSizeProfile ()
  {
    return this.bufferSizeProfile;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // WAIT/SERVICE TIME PROFILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final DoubleParameterProfile waitServiceTimeProfile;
  
  public final DoubleParameterProfile getWaitServiceTimeProfile ()
  {
    return this.waitServiceTimeProfile;
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
    final IntegerParameterProfile numberOfServersProfile,
    final IntegerParameterProfile bufferSizeProfile,
    final DoubleParameterProfile waitServiceTimeProfile)
  {
    this.queueClass = queueClass;
    this.defaultName = defaultName;
    this.composite = composite;
    this.generatorProfile = generatorProfile;
    this.numberOfServersProfile = numberOfServersProfile;
    this.bufferSizeProfile = bufferSizeProfile;
    this.waitServiceTimeProfile = waitServiceTimeProfile;
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
    
    public double startTime = Double.NEGATIVE_INFINITY;
    
  }
  
}
