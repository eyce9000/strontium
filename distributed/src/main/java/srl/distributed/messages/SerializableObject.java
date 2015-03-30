package srl.distributed.messages;

import java.io.Serializable;

import org.simpleframework.xml.Root;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Root(strict=false)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@type")
public abstract class SerializableObject implements Serializable{

}
