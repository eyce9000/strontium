/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2012 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&M University 
 *        nor the names of its contributors may be used to endorse or promote 
 *        products derived from this software without specific prior written 
 *        permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *  
 *******************************************************************************/
package srl.tools;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generate all subsets of 0...n-1
 * @author mfield
 *
 */
public class SubsetGenerator implements Iterator<List<Integer>>, Iterable<List<Integer>>{

        int n;
        List<Integer> last = null;
        
        public SubsetGenerator(int n)
        {
                this.n = n;
        }
        
        @Override
        public boolean hasNext() {
                return last == null || last.size() < n;
        }

        /**
         * Increment arr[idx].
         * @param arr
         * @param idx
         */
        public static void increment(ArrayList<Integer> arr, int idx)
        {
                arr.set(idx, arr.get(idx)+1);
        }
        
        @Override
        public List<Integer> next() {
                if (last == null)
                        return last = new ArrayList<Integer>();
                
                ArrayList<Integer> res = new ArrayList<Integer>(last);
                int size = last.size();
                
                if (size == 0)
                        res.add(0);
                else if (res.get(size-1) < n-1)
                        increment(res, size-1);
                else
                {
                        int i = size-2;
                        while (i >= 0 && res.get(i) == res.get(i+1)-1)
                                --i;
                        
                        if (i < 0)
                        {
                                res.clear();
                                for (i = 0; i < size+1; ++i)
                                        res.add(i);
                        }
                        else
                        {
                                increment(res, i);
                                for (int idx = 1; i+idx < res.size(); ++idx)
                                        res.set(i+idx, res.get(i)+idx);
                        }
                }
                
                return last = res;
        }

        @Override
        public void remove() {
                throw new UnsupportedOperationException("This just doesn't make sense");
        }

        @Override
        public Iterator<List<Integer>> iterator() {
                return this;
        }
        
        public static void main(String[] args)
        {
                int n = 6;
                SubsetGenerator sg = new SubsetGenerator(n);
                int ls = 0, i = 0;
                for (List<Integer> li: sg)
                {
                        ++i;
                        if (li.size() != ls)
                        {
                                System.out.print("\n");
                                ls = li.size();
                        }
                        System.out.print(li);
                }
                System.out.print("\ntotal: " + i + " out of: " + (int)Math.pow(2,n) + "\n");
        }

}

