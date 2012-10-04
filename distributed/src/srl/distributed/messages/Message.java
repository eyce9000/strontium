package srl.distributed.messages;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.simpleframework.xml.Root;

@Root(strict=false)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@type")
public abstract class Message {

}
