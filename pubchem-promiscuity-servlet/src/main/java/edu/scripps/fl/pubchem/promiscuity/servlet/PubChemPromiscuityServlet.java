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
package edu.scripps.fl.pubchem.promiscuity.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.scripps.fl.pubchem.promiscuity.model.PCPromiscuityParameters;
import edu.scripps.fl.queue.QueueServlet;

public class PubChemPromiscuityServlet extends HttpServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String compoundIdString = req.getParameter("area");
		String[] compoundIdSArray = compoundIdString.split("\\D+");
		List<Long> compoundIds = new ArrayList<Long>(compoundIdSArray.length);
		for (int ii = 0; ii < compoundIdSArray.length; ii++) {
			String compoundIdSt = compoundIdSArray[ii];
			if(null != compoundIdSt && !compoundIdSt.equals(""))
				compoundIds.add(Long.parseLong(compoundIdSt));
		}

		String db = req.getParameter("database");
		Boolean isSimple = true;
		if (req.getParameter("simple").equals("false"))
			isSimple = false;

		Boolean perProtein = false;
		if (req.getParameter("perProtein").equals("true"))
			perProtein = true;

		PCPromiscuityParameters params = new PCPromiscuityParameters();
		params.setIds(compoundIds);
		params.setDatabase(db);
		params.setSimpleMode(isSimple);
		params.setPerProteinMode(perProtein);

		File dir = new File(getServletContext().getRealPath("/output"));
		dir.mkdirs();
		File tmpFile = File.createTempFile("pcpromiscuity", ".xml", dir);

		PCPromiscuityRunnable runnable = new PCPromiscuityRunnable(params, tmpFile);
		Future<?> future = QueueServlet.executor.submit(runnable);
		String uniqueId = "output/" + tmpFile.getName();
		QueueServlet.queueMap.put(uniqueId, future);
		String redirectHtml = "";
		if(isSimple){
			if(perProtein)
				redirectHtml = "PerCompoundAndProtein_Simple.html"; 
			else
				redirectHtml = "PerCompound_Simple.html";
		}
		else{
			if(perProtein)
				redirectHtml = "PerCompoundAndProtein_Advanced.html";
			else
				redirectHtml = "PerCompound_Advanced.html";
		}
		String redirectURL = String.format("/pubchem/%s?id=%s", redirectHtml, uniqueId);
		QueueServlet.redirectMap.put(uniqueId, redirectURL);
		resp.sendRedirect(resp.encodeRedirectURL("/pubchem/queue.html?" + "id=" + uniqueId));
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
