package srl.core.util.lists;


/** 
 * Implements a disjoint set data structure, implementing union/find for 
 * finding equivalence classes.  Note that this structure does not hold the 
 * objects themselves, but just a collection of integers representing 
 * each of the objects.
 * 
 * $Id: DisjointSet.java,v 1.1 2007-11-11 20:54:54 hammond Exp $
 * @author Tracy Hammond <hammond@ai.mit.edu>
 * copyright (c) MIT, Tracy Hammond, Design Rationale Group at MIT
 * Created: <Wed 2004-06-29 15:27:38>
 **/
public class DisjointSet{ 

  private int[] m_set;

  /**
   * Construct the disjoint sets object
   * @param numElements the initial number of disjoint sets
   **/
  public DisjointSet(int numElements){
    m_set = new int[numElements];
    for(int i = 0; i < m_set.length; i++){
      m_set[i] = -1;
    }
  }

  /**
   * union two disjoint sets
   * @param root1 the root of set1
   * @param root2 the root of set2
   **/
  public void union(int root1, int root2){
    if(root1 == root2) return;
    if(find(root2) != root2){
      union(find(root2), root1);
    }
    m_set[root2] = find(root1);
  }

  /**
   * find the root of a set
   * @param x the element number being searched for
   * @return the root set containing x
   **/
  public int find(int x){
    if(m_set[x] < 0)
      return x;
    return m_set[x] = find(m_set[x]);
  }

  public void print(){
    for(int i = 0; i < m_set.length; i++){
      System.out.println(i + ": " + m_set[i]);
    }
  }
}
