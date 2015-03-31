package srl.core.serialization;

import org.openawt.Color;
import org.openawt.svg.Style;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class SketchModule extends SimpleModule {
	public SketchModule(){
		super("SketchModule", new Version(1,0,0,null));
	}
	
	@Override
	public void setupModule(SetupContext context){
		context.setMixInAnnotations(Color.class, TypeAttributeMixin.class);
		context.setMixInAnnotations(Style.class, TypeAttributeMixin.class);
	}
}
