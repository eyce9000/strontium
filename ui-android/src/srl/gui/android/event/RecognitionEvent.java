package srl.gui.android.event;

import srl.core.sketch.Sketch;

public class RecognitionEvent {
	Sketch sketch;
	public RecognitionEvent(Sketch sketch){
		this.sketch = sketch;
	}
}
