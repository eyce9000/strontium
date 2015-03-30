package srl.distributed;

import com.fasterxml.jackson.databind.ObjectMapper;


public interface ObjectMapperProvider {
	public ObjectMapper getMapper();
}
