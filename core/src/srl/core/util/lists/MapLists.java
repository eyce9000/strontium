/*
 * Created on Aug 31, 2005 by moltmans
 * Copyright: Copyright (C) 2005 by MIT.  All rights reserved.
 * Version: $Id: MapLists.java,v 1.1 2007-11-11 20:54:54 hammond Exp $
 */
package srl.core.util.lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This class ia a map from a key to a list of objects. New lists are created on
 * demand as things are added to them.
 * 
 * Note that MapSets extends this class, and should be examined when making
 * any changes to this class.
 * 
 * @author Mike Oltmans
 * 
 */
public class MapLists<K,V> {

  private HashMap<K,ArrayList<V>> m_map;
  
  public MapLists() {
    m_map = new HashMap<K, ArrayList<V>>();
  }
  
  /**
   * Add a the value to the list of entries for key
   * 
   * @param key
   *        The key to add
   * @param value
   *        The value to add to the list of values associated with the key
   */
  public void add(K key, V value) {
    internalGetValue(key).add(value);
  }
  
  public void combine(MapLists<K,V> ml){
    for(K key : ml.keySet()){
      add(key, ml.getValues(key));
    }
  }
  
  /**
   * Remove the given value from the given key's list. If the last element of
   * the value list is removed, remove the key from the map.
   * 
   * @param key
   *        The key that holds the value
   * @param value
   *        The value within the key's list to remove
   * @return true if the list of values is modified.
   */
  public boolean remove(K key, V value) {
    if (!m_map.containsKey(key)) {
      return false;
    }
    ArrayList<V> list = getValues(key);
    boolean removed = list.remove(value);
    if (list.isEmpty()) {
      m_map.remove(key);
    }
    return removed;
  }
  
  /**
   * Remove all of the values for the given key
   * @see java.util.HashMap#remove(java.lang.Object)
   */
  public ArrayList<V> remove(K key) {
    return m_map.remove(key);
  }
  
  /**
   * @see java.util.HashMap#clear()
   */
  public void clear() {
    m_map.clear();
  }

  /**
   * @see java.util.HashMap#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    return m_map.containsKey(key);
  }

  /**
   * @see java.util.HashMap#containsKey(java.lang.Object)
   */
  public boolean contains(K key, V value) {
    return getValues(key).contains(value);
  }
  
  /**
   * @see java.util.HashMap#entrySet()
   */
  public Set<Entry<K, ArrayList<V>>> entrySet() {
    return m_map.entrySet();
  }

  /**
   * @see java.util.HashMap#isEmpty()
   */
  public boolean isEmpty() {
    return m_map.isEmpty();
  }

  /**
   * @see java.util.HashMap#size()
   */
  public int size() {
    return m_map.size();
  }

  protected ArrayList<V> internalGetValue(K key) {
    ArrayList<V> list = m_map.get(key);
    if (list == null) {
      list = new ArrayList<V>();
      m_map.put(key, list);
    }
    return list;
  }
  
  /**
   * Get the list of values for the key.
   * 
   * @param key
   * @return A list (possibly length==0 but never null)
   */
  public ArrayList<V> getValues(K key) {
    ArrayList<V> list = m_map.get(key);
    if (list == null) {
      list = new ArrayList<V>();
    }
    return list;
  }
  
  public Set<K> keySet() {
    return m_map.keySet();
  }  
  
  /**
   * Get a collection of the lists from this map
   * 
   * @return The collection of lists.
   */
  public Collection<ArrayList<V>> valueSet() {
    return m_map.values();
  }

  /**
   * @param key
   * @param values
   */
  public void add(K key, ArrayList<V> values) {
    internalGetValue(key).addAll(values);
  }

  /**
   * @param key
   * @param values
   */
  public void retainAll(K key, ArrayList<V> values) {
     getValues(key).retainAll(values);
  }
  
  public void print(){  
    for(K key : m_map.keySet()){
      System.out.println("  " + key + ": ");
      for(V value: getValues(key)){
        System.out.println("    " + value + " ");
      }
    }
  }
  
}
