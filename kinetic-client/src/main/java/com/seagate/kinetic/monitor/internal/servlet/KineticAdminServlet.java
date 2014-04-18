/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.monitor.internal.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seagate.kinetic.monitor.internal.service.KineticHeartbeatListenerService;

/**
 * Servlet implementation class KineticAdminServlet
 */
@WebServlet("/KineticAdminServlet")
public class KineticAdminServlet extends HttpServlet {
	private final static Logger logger = Logger
			.getLogger(KineticAdminServlet.class.getName());

	private static final long serialVersionUID = 1L;
	private KineticHeartbeatListenerService kineticHeartbeatListenerService;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KineticAdminServlet() {
		super();
	}

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		long unavailableThreshold = Long.parseLong(servletConfig
				.getInitParameter("unavailableThreshold"));
		try {
			kineticHeartbeatListenerService = new KineticHeartbeatListenerService(
					unavailableThreshold);
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		Writer writer = response.getWriter();

		action = action == null ? "listNodesAbstract" : action;

		if (action.equals("listNodesAbstract")) {
			writer.write(kineticHeartbeatListenerService
					.listNodesAbstractAsJson());
		} else if (action.equals("getNodeDetails")) {
			String node = request.getParameter("node");
			writer.write(kineticHeartbeatListenerService
					.getNodeDetailsAsJson(node));
		} else if (action.equals("listNodesDetails")) {
			writer.write(kineticHeartbeatListenerService
					.listNodesDetailAsJson());
		}

		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
