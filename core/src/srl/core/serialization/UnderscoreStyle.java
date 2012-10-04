package srl.core.serialization;

import org.simpleframework.xml.stream.Style;

public class UnderscoreStyle implements Style{

	@Override
	public String getAttribute(String attr) {
		return toUnderscoreStyle(attr);
	}

	@Override
	public String getElement(String el) {
		return toUnderscoreStyle(el);
	}
	
	private String toUnderscoreStyle(String in){
		//Split on upper case
		return in.replaceAll("(\\p{Ll})(\\p{Lu})","$1_$2").toLowerCase();
	}
}
