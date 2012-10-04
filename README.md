Strontium Library (SrL)
----------------------
The Strontium Library is a collection of sketch recognition libraries.


Simple Example
-------
The following example shows how to create a sketch object, add a stroke made up of points and perform basic shape recognition on it:
```````java
	Sketch sketch = new Sketch();
	Stroke stroke = new Stroke();
	for(int i=0; i<20; i++){
	  stroke.add(new Point(i,i));
	}
	
	sketch.add(stroke);	
	
	PaleoSketchRecognizer recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
	IRecognition result = recognizer.recognize(sketch.getFirstStroke());
	
	if(result.getBestShape().label.equalsLowerCase("line"))
	  System.out.println("Correctly recognized as a line");
```````
