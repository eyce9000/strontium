
package srl.core.util.lists;

import java.util.List;


/**
 * This class ia a map from a key to a list of objects. New lists are created on
 * demand as things are added to them. Duplicate objects do not exist in this class.
 * 
 * @author Tracy Hammond
 * 
 */
public class MapSets<K,V> extends MapLists<K,V>{
  
  public MapSets() {
    super();
  }
  
  /**
   * Add the value to the list of entries for key
   * 
   * @param key
   *        The key to add
   * @param value
   *        The value to add to the list of values associated with the key
   */
  @Override
  public void add(K key, V value) {
    if(containsKey(key)){
      List<V> v = getValues(key);
      if(v.contains(value)){return;}
    }
    internalGetValue(key).add(value);
  }
  
}
