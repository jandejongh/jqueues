package nl.jdj.jqueues.r5.util.collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

/** A <code>java.util.HashMap</code> with ordered value set and extended with methods to obtain pre-images of (sets of) values.
 *
 * <p>
 * Note that <code>null</code> values are not allowed in this implementation.
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
public class HashMapWithPreImageAndOrderedValueSet<K, V>
extends HashMap<K, V>
{

  private /* final */ TreeMap<V, Set<K>> preImage = new TreeMap<> ();
  
  @Override
  public HashMapWithPreImageAndOrderedValueSet clone ()
  {
    final HashMapWithPreImageAndOrderedValueSet treeMapWithPreImage = (HashMapWithPreImageAndOrderedValueSet) super.clone ();
    treeMapWithPreImage.preImage = new TreeMap (this.preImage);
    return treeMapWithPreImage;
  }

  @Override
  public void clear ()
  {
    super.clear ();
    this.preImage.clear ();
  }

  private /* final */ void removePreImage (final Object key, final V value)
  {
    if (key == null || value == null)
      throw new NullPointerException ();
    if (! (this.preImage.containsKey (value)
        && this.preImage.get (value) != null
        && this.preImage.get (value).contains (key)))
      throw new RuntimeException ();
    this.preImage.get (value).remove (key);
    if (this.preImage.get (value).isEmpty ())
      this.preImage.remove (value);
  }
  
  @Override
  public V remove (Object key)
  {
    if (containsKey (null))
      throw new RuntimeException ();
    final V oldValue = super.remove (key);
    if (oldValue != null)
      removePreImage (key, oldValue);
    return oldValue;
  }

  @Override
  public V put (K key, V value)
  {
    if (containsKey (null))
      throw new RuntimeException ();
    if (key == null || value == null)
      throw new NullPointerException ();
    final V oldValue = super.put (key, value);
    if (oldValue != null)
      removePreImage (key, oldValue);
    if (! this.preImage.containsKey (value))
      this.preImage.put (value, new HashSet<K> ());
    this.preImage.get (value).add (key);
    return oldValue;
  }

  @Override
  public void putAll (Map<? extends K, ? extends V> map)
  {
    if (containsKey (null))
      throw new RuntimeException ();
    if (map == null || map.containsKey (null) || map.containsValue (null))
      throw new NullPointerException ();
    for (Map.Entry<? extends K, ? extends V> me : map.entrySet ())
      put (me.getKey (), me.getValue ());
  }
  
  public V firstValue () throws NoSuchElementException
  {
    return this.preImage.firstKey ();
  }
  
  public Set<K> getPreImageForValue (V value)
  {
    if (containsKey (null))
      throw new RuntimeException ();
    final Set<K> keys = this.preImage.get (value);
    if (keys != null)
      return new LinkedHashSet<> (keys);
    else
      return new LinkedHashSet<> ();
  }
  
  public Set<K> getPreImageForSet (Set<V> values)
  {
    if (containsKey (null))
      throw new RuntimeException ();
    if (values == null)
      throw new NullPointerException ();
    final Set<K> preImage = new LinkedHashSet<> ();
    for (V value : values)
      preImage.addAll (getPreImageForValue (value));
    return preImage;
  }
  
}
