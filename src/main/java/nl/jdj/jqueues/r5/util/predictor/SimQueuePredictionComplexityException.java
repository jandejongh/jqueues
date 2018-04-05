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
package nl.jdj.jqueues.r5.util.predictor;

/** Thrown to indicate that a {@link SimQueuePredictor} or related object cannot produce one or more predictions
 *  because a certain complexity threshold (e.g., in terms of number of predictions) is exceeded.
 * 
 * <p>This implementation merely mimics the constructors from its superclass.
 * The constructors are therefore undocumented; see {@link Exception} for their semantics.
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
public class SimQueuePredictionComplexityException
extends SimQueuePredictionException
{
  
  public SimQueuePredictionComplexityException ()
  {
    super ();
  }
  
  public SimQueuePredictionComplexityException (final String message)
  {
    super (message);
  }
  
  public SimQueuePredictionComplexityException (final String message, final Throwable cause)
  {
    super (message, cause);
  }
  
  public SimQueuePredictionComplexityException (final Throwable cause)
  {
    super (cause);
  }
  
}
