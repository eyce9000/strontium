package srl.core.serialization;

import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="@type")
public class TypeAttributeMixin {
	
}
