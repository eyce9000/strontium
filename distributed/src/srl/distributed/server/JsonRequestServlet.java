/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2012 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&M University 
 *        nor the names of its contributors may be used to endorse or promote 
 *        products derived from this software without specific prior written 
 *        permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *  
 *******************************************************************************/
package srl.distributed.server;

import java.io.IOException;

import javax.servlet.http.*;

import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import srl.distributed.DefaultMapperProvider;
import srl.distributed.ObjectMapperProvider;
import srl.distributed.messages.ErrorResponse;
import srl.distributed.messages.Request;
import srl.distributed.messages.Response;
import srl.distributed.messages.UnauthorizedResponse;

@SuppressWarnings("serial")
public class JsonRequestServlet extends HttpServlet{
	private boolean debugActive;
	private ObjectMapperProvider mapperProvider = new DefaultMapperProvider();

	public void setDebugMode(boolean debug){
		this.debugActive = debug;
	}
	
	public ObjectMapperProvider getObjectMapperProvider(){
		return mapperProvider;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp){
		setDebugMode(true);


		ObjectMapper mapper = getObjectMapperProvider().getMapper();
		
		HttpSession session = req.getSession(true);
		session.getId();

		session.setAttribute("request_path", req.getServletPath());

		try {
			Response responseMessage;
			try{
				ServerRequest requestMessage = mapper.readValue(req.getInputStream(), ServerRequest.class);

				responseMessage = requestMessage.performService(new ServiceContext(req));
			} 
			catch (Throwable e){
				if(debugActive){
					responseMessage = new ErrorResponse(e);
					e.printStackTrace();
				}
				else{
					responseMessage = new ErrorResponse(e.getMessage());
				}
			}
			if(responseMessage instanceof UnauthorizedResponse){
				resp.sendError(403);
			}
			else{
				resp.setContentType("application/json");
				mapper.writeValue(resp.getOutputStream(), responseMessage);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
