/*
 * Created on Oct 12, 2005 by hammond
 * Copyright: Copyright (C) 2005 by MIT.  All rights reserved.
 * Version: $Id: SortedMapLists.java,v 1.1 2007-11-16 02:05:55 hammond Exp $
 */
package srl.core.util.lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * This class ia a map from a key to a list of objects. New lists are created on
 * demand as things are added to them.
 * 
 * @author Mike Oltmans
 * 
 */
public class SortedMapLists<K,V> {

  private SortedMap<K,ArrayList<V>> m_map;
  
  public SortedMapLists() {
    m_map = new TreeMap<K, ArrayList<V>>();
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
  
  public void add(K key, ArrayList<V> values){
    for(V value: values){
      add(key, value);
    }
  }
  
  public void combine(SortedMapLists<K,V> ml){
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

  private ArrayList<V> internalGetValue(K key) {
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
  
  public ArrayList<V> getValues(K fromKey, K toKey){
    ArrayList<V> out = new ArrayList<V>();
    for(ArrayList<V> l : m_map.subMap(fromKey, toKey).values()){
      out.addAll(l);
    }
    return out;
  }
  
  public ArrayList<V> getTail(K fromKey){
    ArrayList<V> out = new ArrayList<V>();
    for(ArrayList<V> l : m_map.tailMap(fromKey).values()){
      out.addAll(l);
    }
    return out;
  }
  
  public ArrayList<V> getHead(K toKey){
    ArrayList<V> out = new ArrayList<V>();
    for(ArrayList<V> l : m_map.headMap(toKey).values()){
      out.addAll(l);
    }
    return out;
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
  
  public void print(){
    for(K key : m_map.keySet()){
      System.out.println("  " + key + ": ");
      for(V value: getValues(key)){
        System.out.println("    " + value + " ");
      }
    }
  }
}
