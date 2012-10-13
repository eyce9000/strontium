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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class PermutationGenerator implements Iterator<List<Integer>>, Iterable<List<Integer>> {

        int size;
        List<Integer> last = null;
        
        public PermutationGenerator(int i)
        {
                size = i;
        }
        
        @Override
        public boolean hasNext() {
                if (last == null) return true;
                for (int i = 1; i < last.size(); ++i)
                {
                        if (last.get(i) > last.get(i-1))
                                return true;
                }
                return false;
        }

        @Override
        public List<Integer> next() {
                if (!hasNext())
                        throw new NoSuchElementException("out of permutations");
                
                if (last == null)
                {
                        last = new ArrayList<Integer>();
                        for (int i = 0; i < size; ++i)
                                last.add(i);
                        return last;
                }
                
                int j = last.size()-2;
                while (last.get(j) > last.get(j+1))
                        --j;
                
                int k = last.size()-1;
                while (last.get(j) > last.get(k))
                        --k;
                
                Collections.swap(last, j, k);
                
                k = last.size()-1;
                ++j;
                
                while (k > j)
                {
                        Collections.swap(last, k, j);
                        --k;
                        ++j;
                }
                
                return last;
        }

        @Override
        public void remove() {
                throw new UnsupportedOperationException("this just doesn't make sense");
        }
        
        public static void main(String[] args)
        {
                PermutationGenerator pg = new PermutationGenerator(5);
                while (pg.hasNext())
                        System.out.println(pg.next());
        }

        @Override
        public Iterator<List<Integer>> iterator() {
                return this;
        }

}

