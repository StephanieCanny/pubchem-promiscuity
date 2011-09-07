/*					
 * Copyright 2011 The Scripps Research Institute					
 *					
 * Licensed under the Apache License, Version 2.0 (the "License");					
 * you may not use this file except in compliance with the License.					
 * You may obtain a copy of the License at					
 *					
 *     http://www.apache.org/licenses/LICENSE-2.0					
 *					
 * Unless required by applicable law or agreed to in writing, software					
 * distributed under the License is distributed on an "AS IS" BASIS,					
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.					
 * See the License for the specific language governing permissions and					
 * limitations under the License.					
 */
package edu.scripps.fl.queue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueServlet extends HttpServlet {

	public static ExecutorService executor = Executors.newFixedThreadPool(3);
	public static Map<Object, Future<?>> queueMap = new HashMap<Object, Future<?>>();
	private static final Logger log = LoggerFactory.getLogger(QueueServlet.class);
	public static Map<Object, String> redirectMap = new HashMap<Object, String>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Boolean finished = false;
		Object uniqueId = req.getParameter("id");

		Future future = queueMap.get(uniqueId);

		if (null == future) {
			resp.sendRedirect(resp.encodeRedirectURL("/pubchem/missing.html"));
		} else if (future.isDone()) {
			queueMap.remove(uniqueId);
			log.info(uniqueId.toString());
			String rUrl = redirectMap.get(uniqueId);
			redirectMap.remove(uniqueId);
			resp.sendRedirect(resp.encodeRedirectURL(rUrl));
		} else {
			resp.sendRedirect(resp.encodeRedirectURL("/pubchem/queue.html?" + "id=" + uniqueId));
		}
	}

//	try catch?
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
