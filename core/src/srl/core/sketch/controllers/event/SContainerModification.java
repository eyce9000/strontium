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
