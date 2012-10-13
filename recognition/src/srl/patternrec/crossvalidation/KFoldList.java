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
package srl.patternrec.crossvalidation;

import java.util.ArrayList;
import java.util.List;

public class KFoldList<T> {
	private List<List<T>> testing;
	private List<List<T>> training;
	private int k = 0;
	public KFoldList(List<T> values, int k){
		this.k = k;
		testing = new ArrayList<List<T>>();
		training = new ArrayList<List<T>>();
		
		int size = values.size();
		
		
		
		
		int total = 0;
		for(int i=0; i<k; i++){
			int width = size/k;
			if(i==k-1){
				width = size - total;
			}
			List<T> selectedTesting = new ArrayList<T>(values.subList(total, total + width));
			List<T> selectedTraining = new ArrayList<T>();
			if(total>0)
				selectedTraining.addAll(values.subList(0, total));
			if(total+width<size)
				selectedTraining.addAll(values.subList(total+width, size));
			total += width;
			
			testing.add(selectedTesting);
			training.add(selectedTraining);
		}
	}
	public int getK(){
		return k;
	}
	public List<T> getTesting(int fold){
		return testing.get(fold);
	}
	public List<T> getTraining(int fold){
		return training.get(fold);
	}
}
