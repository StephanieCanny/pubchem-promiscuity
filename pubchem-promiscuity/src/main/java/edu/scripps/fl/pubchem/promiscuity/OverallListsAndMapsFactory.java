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
package edu.scripps.fl.pubchem.promiscuity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.promiscuity.model.Protein;
import edu.scripps.fl.pubchem.web.ELinkResult;
import edu.scripps.fl.pubchem.web.entrez.EUtilsFactory;

public class OverallListsAndMapsFactory {

	private static final Logger log = LoggerFactory.getLogger(OverallListsAndMapsFactory.class);

	public List<Long> aidListEsearch(List<Long> ids, String searchTerm, String db) throws Exception {
		List<Long> newAids = new ArrayList<Long>();
		EUtilsFactory factory = new EUtilsFactory();
		StringBuffer query = new StringBuffer();
		query.append("(" + StringUtils.join(ids, "[" + db + "IdTested] OR "));
		query.append("[" + db + "IdTested]) AND " + searchTerm);
		newAids = factory.getIds(query.toString(), "pcassay");
		return newAids;
	}

	public List<Long> aidtoAIDListEsearch(List<Long> aids, String searchTerm) throws Exception {
		List<Long> newAids = new ArrayList<Long>();
		EUtilsFactory factory = new EUtilsFactory();
		StringBuffer query = new StringBuffer();
		query.append("(" + StringUtils.join(aids, "[uid] OR "));
		query.append("[uid]) AND " + searchTerm);
		newAids = factory.getIds(query.toString(), "pcassay");
		return newAids;
	}

	public Set<Protein> allProteinSet(Map<Long, List<Protein>> aidProteinMap) {
		Set<Protein> allProteinSet = new HashSet<Protein>();
		for (Long ll : aidProteinMap.keySet()) {
			List<Protein> proteins = aidProteinMap.get(ll);
			if (proteins.size() > 0)
				allProteinSet.addAll(proteins);
		}
		return allProteinSet;
	}

	public Map<Long, List<Protein>> getAIDProteinMap(List<Long> aids) throws Exception {
		log.info("Number of aids in eSummary request: " + aids.size());
		log.info("Memory usage before getting aid eSummary document: " + memUsage());
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		AssayESummaryHandler handler = new AssayESummaryHandler();
		InputStream is = EUtilsFactory.getInstance().getSummaries(aids, "pcassay");
		saxParser.parse(is, handler);
		log.info("Memory usage after getting aid eSummary document: " + memUsage());
		return handler.getMap();
	}

	public MultiValueMap getSummaryToAIDsMap(Map<Long, List<ELinkResult>> aidToSummaryMap) {
		MultiValueMap map = new MultiValueMap();
		log.info("Memory used before summary to aids map: " + memUsage());
		log.info("Setting up summary to AID map");
		for (Long ll : aidToSummaryMap.keySet()) {
			ELinkResult result = (ELinkResult) aidToSummaryMap.get(ll);
			if (result == null)
				continue;
			List<Long> summaries = result.getIds("pcassay", "pcassay_pcassay_neighbor_list");
			if (summaries != null) {
				for (Long summary : summaries) {
					map.put(summary, ll);
				}
			}
		}
		log.info("Finished setting up summary to AID map");
		log.info("Memory used after summary to aids map: " + memUsage());
		return map;
	}

	private long memUsage() {
		long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		return mem / 1024 / 1024;
	}

	public Map<Protein, List<Long>> proteinAIDMap(Map<Long, List<Protein>> aidProteinMap, Set<Protein> allProteins) {
		Map<Protein, List<Long>> proteinAIDMap = new HashMap<Protein, List<Long>>();
		for (Protein protein : allProteins) {
			List<Long> aids = new ArrayList<Long>();
			for (Long aid : aidProteinMap.keySet()) {
				List<Protein> aidProteins = aidProteinMap.get(aid);
				if (aidProteins != null) {
					if (aidProteins.contains(protein))
						aids.add(aid);
				}
			}
			proteinAIDMap.put(protein, aids);
		}
		return proteinAIDMap;
	}

}
