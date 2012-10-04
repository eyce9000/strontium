Strontium Library (SrL)
----------------------
The Strontium Library is a collection of sketch recognition libraries.


Simple Example
-------
The following example shows how to create a sketch object, add a stroke made up of points and perform basic shape recognition on it:
```````java
	/* 
	 Create the sketch. This would normally be done by collection points from user interaction
	*/
	Sketch sketch = new Sketch();
	
	Stroke stroke = new Stroke();
	for(int i=0; i<20; i++){
	  stroke.add(new Point(i,i));
	}
	
	sketch.add(stroke);	
	
	/*
	 Run basic shape recognition on the first stroke of the sketch (the one we just created)
	 This should result in a best shape label of "Line"
	*/
	PaleoSketchRecognizer recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
	IRecognition result = recognizer.recognize(sketch.getFirstStroke());
	
	if(result.getBestShape().label.equalsLowerCase("line"))
	  System.out.println("Correctly recognized as a line");
```````

Configuring PaleoSketch
-----------------------
To instantiate the PaleoSketchRecognizer you must first create a PaleoConfig. This allows you to specify which set of shapes you would like to recognize.

`````````java
	/*
	 By default, every shape is enabled when using the plain constructor
	*/
	PaleoConfig config = new PaleoConfig();

	/*
	 Or you can pass a list of shape Options to specify which to enable. All other shapes will be disabled.
	*/
	config = new PaleoConfig(PaleoConfig.Option.Line, PaleoConfig.Option.Circle, PaleoConfig.Option.Polyline);

	/*
	 Or you can use one of several predefined recognition sets.
	*/
	config = PaleoConfig.allOn();
	config = PaleoConfig.basicPrimsOnly();
`````````java

