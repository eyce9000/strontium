package srl.distributed.server;


import java.io.IOException;

import javax.servlet.http.*;

import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import srl.distributed.Serialize;
import srl.distributed.messages.ErrorResponse;
import srl.distributed.messages.Request;
import srl.distributed.messages.Response;
import srl.distributed.messages.UnauthorizedResponse;



@SuppressWarnings("serial")
public class JsonRequestServlet extends HttpServlet{
	private boolean debugActive;


	public void setDebugMode(boolean debug){
		this.debugActive = debug;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp){
		setDebugMode(true);


		ObjectMapper mapper = Serialize.buildMapper();
		
		HttpSession session = req.getSession(true);
		session.getId();

		session.setAttribute("request_path", req.getServletPath());

		try {
			Response responseMessage;
			try{
				Request requestMessage = mapper.readValue(req.getInputStream(), Request.class);

				responseMessage = requestMessage.performService(session);
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
