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
package srl.core.sketch.controllers.event;

import java.util.ArrayList;
import java.util.List;

import srl.core.sketch.SComponent;
import srl.core.sketch.SContainer;
import srl.core.sketch.Sketch;

public class SContainerModification implements Modification<SContainer>{
	private List<SComponent> added = new ArrayList<SComponent>();
	private List<SComponent> removed = new ArrayList<SComponent>();
	private List<SComponent> modified = new ArrayList<SComponent>();
	private long timestamp;
	public SContainerModification(){
		timestamp = System.currentTimeMillis();
	}
	public SContainerModification(List<SComponent> added,List<SComponent> removed){
		this();
		this.added = added;
		this.removed = removed;
	}
	public SContainerModification(SContainer original, SContainer modified){
		this();
		
		for(SComponent comp:original){
			SComponent other = modified.get(comp.getId());
			if(other==null)
				removed.add(comp);	
			else if(!other.equalsByContent(comp))
				modified.add(other);
				
		}
		for(SComponent comp:modified){
			if(!original.contains(comp.getId(), false))
				added.add(comp);	
		}
	}
	public void appendRemoved(SComponent removedComponent){
		removed.add(removedComponent);
		timestamp = System.currentTimeMillis();
	}
	public void appendAdded(SComponent addedComponent){
		added.add(addedComponent);
		timestamp = System.currentTimeMillis();
	}
	@Override
	public long getTimestamp() {
		return timestamp;
	}
}
