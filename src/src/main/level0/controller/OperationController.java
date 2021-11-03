package src.main.level0.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/level0")
public class OperationController {

	private static final Log LOG = LogFactory.getLog(OperationController.class);

	@Autowired
	@Qualifier("level0Handlers")
	private List<Handler> handlers;

	@Autowired
	@Qualifier("unknownCommandHandler")
	private Handler defaultHandler;

	@SuppressWarnings("deprecation")
	@PostMapping(value = "operation", consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_UTF8_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public void doOperation(HttpServletRequest request, HttpServletResponse response) {
		try {
			final String payload = IOUtils.toString(request.getInputStream(), Charset.forName("UTF-8"));
			response.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			for (final Handler handler : handlers) {
				if (handler.accept(payload)) {
					response.getWriter().print(handler.handle(payload));
					return;
				}
			}
			response.getWriter().print(defaultHandler.handle(payload));
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}
}
