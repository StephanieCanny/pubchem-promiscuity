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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.scripps.fl.pubchem.promiscuity.model.PCPromiscuityParameters;
import edu.scripps.fl.pubchem.web.entrez.EUtilsWebSession;
import edu.scripps.fl.queue.QueueServlet;

public class PubChemPromiscuityServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        int threads = Integer.parseInt( config.getInitParameter("threads") );
        QueueServlet.executor = Executors.newFixedThreadPool(threads);
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String compoundIdString = req.getParameter("ids");
        String[] compoundIdSArray = compoundIdString.split("\\D+");
        List<Long> compoundIds = new ArrayList<Long>(compoundIdSArray.length);
        for (int ii = 0; ii < compoundIdSArray.length; ii++) {
            String compoundIdSt = compoundIdSArray[ii];
            if(null != compoundIdSt && !compoundIdSt.equals(""))
                compoundIds.add(Long.parseLong(compoundIdSt));
        }
        
        // Set email for ncbi web services requests
        String email = req.getParameter("email");
        EUtilsWebSession.setEmail(email);
        
        String db = req.getParameter("database");
        Boolean isSimple = true;
        Boolean perProtein = false;
        String redirectHtml = "";
        String searchType = req.getParameter("searchType");
        
        
        
        if (searchType.equals("pcmode_slow")){
            isSimple = false;
            redirectHtml = "PerCompound_Slow.html";
        }
        else{
            perProtein = true;
            if (searchType.equals("ppmode_fast")){
                isSimple = true;
                redirectHtml = "PerCompoundAndProtein_Fast.html"; 
            }
            else if(searchType.equals("ppmode_slow")){
                isSimple = false;
                redirectHtml = "PerCompoundAndProtein_Slow.html";
            }
        }      

        PCPromiscuityParameters params = new PCPromiscuityParameters();
        params.setIds(compoundIds);
        params.setDatabase(db);
        params.setSimpleMode(isSimple);
        params.setPerProteinMode(perProtein);

        File dir = new File(getServletContext().getRealPath("/output"));
        dir.mkdirs();
        File tmpFile = File.createTempFile("pcdata", ".xml", dir);

        PCPromiscuityRunnable runnable = new PCPromiscuityRunnable(params, tmpFile);
        Future<?> future = QueueServlet.executor.submit(runnable);
        String uniqueId = "output/" + tmpFile.getName();
        QueueServlet.queueMap.put(uniqueId, future);
        
        String redirectURL = String.format("/pcpromiscuity/%s?id=%s", redirectHtml, uniqueId);
        QueueServlet.redirectMap.put(uniqueId, redirectURL);
        resp.sendRedirect(resp.encodeRedirectURL("/pcpromiscuity/queue.html?" + "id=" + uniqueId));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

}