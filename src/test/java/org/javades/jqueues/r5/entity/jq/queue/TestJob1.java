/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.javades.jqueues.r5.entity.jq.queue;

import java.util.List;
import java.util.Map;
import org.javades.jqueues.r5.entity.SimEntity;
import org.javades.jqueues.r5.entity.SimEntityEvent;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.job.SimJobListener;
import org.javades.jqueues.r5.misc.example.DefaultExampleSimJob;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/** A test job.
 *  
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public class TestJob1<J extends TestJob1, Q extends SimQueue>
extends DefaultExampleSimJob<J, Q>
implements SimJobListener<J, Q>
{

  public TestJob1 (boolean reported, int n)
  {  
    super (reported, n);
    this.scheduledArrivalTime = this.n;
    registerSimEntityListener (this);
  }

  public final double scheduledArrivalTime;
  
  public boolean arrived = false;

  public boolean started = false;

  public boolean dropped = false;
  
  public boolean revoked = false;

  public boolean departed = false;

  public double arrivalTime = 0.0;

  public double startTime = 0.0;

  public double dropTime = 0.0;

  public double revocationTime = 0.0;
  
  public double departureTime = 0.0;

  public boolean predicted = false;
  
  public boolean predictedArrived = false;

  public boolean predictedStarted = false;

  public boolean predictedDropped = false;

  public boolean predictedRevoked = false;

  public boolean predictedDeparted = false;

  public double predictedArrivalTime = 0.0;

  public double predictedStartTime = 0.0;

  public double predictedDropTime = 0.0;

  public double predictedRevocationTime = 0.0;
    
  public double predictedDepartureTime = 0.0;
    
  public void testPrediction (final double accuracy)
  {
    assert this.predicted;
    assert this.arrived  == this.predictedArrived;
    assert this.started  == this.predictedStarted;
    assert this.dropped  == this.predictedDropped;
    assert this.revoked  == this.predictedRevoked;
    assert this.departed == this.predictedDeparted;
    if (this.arrived)
      assertEquals (this.predictedArrivalTime, this.arrivalTime, accuracy);
    if (this.started)
      assertEquals (this.predictedStartTime, this.startTime, accuracy);
    if (this.dropped)
      assertEquals (this.predictedDropTime, this.dropTime, accuracy);
    if (this.revoked)
      assertEquals (this.predictedRevocationTime, this.revocationTime, accuracy);
    if (this.departed)
      assertEquals (this.predictedDepartureTime, this.departureTime, accuracy);
  }
  
  @Override
  public double getServiceTime (SimQueue queue) throws IllegalArgumentException
  {
    if (queue == null && getQueue () == null)
      return 0.0;
    else
      return (double) n;
  }

  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
  }

  @Override
  public void notifyUpdate (final double time, final SimEntity entity)
  {
  }

  @Override
  public void notifyStateChanged
  (final double time, final SimEntity entity, final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> notifications)
  {
  }

  @Override
  public void notifyArrival (final double time, final J job, final Q queue)
  {
    if (job == null || queue == null)
      throw new IllegalArgumentException ();
    if (job == this)
    {
      if (this.arrived)
        fail ("Already arrived!");
      if (this.started)
        fail ("Arriving after start!");
      if (this.dropped)
        fail ("Arriving after drop!");
      if (this.revoked)
        fail ("Arriving after revocation!");
      if (this.departed)
        fail ("Arriving after departure!");
      this.arrived = true;
      this.arrivalTime = time;
    }
  }

  @Override
  public void notifyStart (final double time, final J job, final Q queue)
  {
    if (job == null || queue == null)
      throw new IllegalArgumentException ();
    if (job == this)
    {
      if (! this.arrived)
        fail ("Starting before arrival!");
      if (this.started)
        fail ("Already started!");
      if (this.dropped)
        fail ("Starting after drop!");
      if (this.revoked)
        fail ("Starting after revocation!");
      if (this.departed)
        fail ("Starting after departure!");
      TestJob1.this.started = true;
      TestJob1.this.startTime = time;
    }
  }

  @Override
  public void notifyDrop (final double time, final J job, final Q queue)
  {
    if (job == null || queue == null)
      throw new IllegalArgumentException ();
    if (job == this)
    {
      if (! this.arrived)
        fail ("Dropped before arrival!");
      if (this.dropped)
        fail ("Already dropped!");
      if (this.revoked)
        fail ("Dropped after revocation!");
      if (this.departed)
        fail ("Dropped after departure!");
      TestJob1.this.dropped = true;
      TestJob1.this.dropTime = time;
    }
  }

  @Override
  public void notifyRevocation (final double time, final J job, final Q queue)
  {
    if (job == null || queue == null)
      throw new IllegalArgumentException ();
    if (job == this)
    {
      if (! this.arrived)
        fail ("Revocation before arrival!");
      if (this.dropped)
        fail ("Revocation after drop!");
      if (this.revoked)
        fail ("Already revoked!");
      if (this.departed)
        fail ("Revocation after departure!");
      TestJob1.this.revoked= true;
      TestJob1.this.revocationTime = time;
    }
  }

  @Override
  public void notifyAutoRevocation (final double time, final J job, final Q queue)
  {
    notifyRevocation (time, job, queue);
  }

  @Override
  public void notifyDeparture (final double time, final J job, final Q queue)
  {
    if (job == null || queue == null)
      throw new IllegalArgumentException ();
    if (job == this)
    {
      if (! this.arrived)
        fail ("Departure before arrival!");
      if (this.dropped)
        fail ("Departure after drop!");
      if (this.revoked)
        fail ("Departure after revocation!");
      if (this.departed)
        fail ("Already departed!");
      this.departed = true;
      this.departureTime = time;
    }
  }

}
