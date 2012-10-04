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

