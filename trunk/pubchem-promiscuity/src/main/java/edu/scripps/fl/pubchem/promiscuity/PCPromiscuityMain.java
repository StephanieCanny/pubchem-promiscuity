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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.scripps.fl.pubchem.promiscuity.model.CompoundPromiscuityInfo;
import edu.scripps.fl.pubchem.promiscuity.model.OverallListsAndMaps;
import edu.scripps.fl.pubchem.promiscuity.model.PCPromiscuityParameters;
import edu.scripps.fl.pubchem.promiscuity.model.PromiscuityCount;
import edu.scripps.fl.pubchem.promiscuity.model.Protein;
import edu.scripps.fl.pubchem.web.ELinkResult;
import edu.scripps.fl.pubchem.web.entrez.ELinkWebSession;

public class PCPromiscuityMain {

	private static String[] searchTerms = {
			"\"NIH Molecular Libraries Program\"[SourceCategory]",
			"\"ChEMBL\"[SourceName]",
			"(\"beta lactamase\"[AssayProtocol] OR \"beta lactamase\"[AssayDescription] "
					+ "OR \"B lactamase\"[AssayDescription] OR \"B lactamase\"[AssayProtocol]) NOT \"Summary\"[ActivityOutcomeMethod]",
			"(\"luciferase\"[AssayProtocol] OR \"luciferase\"[AssayDescription]) NOT \"Summary\"[ActivityOutcomeMethod]",
			"(\"fluorescence\"[AssayProtocol] OR \"fluorescent\"[AssayProtocol] OR \"fluorescence\"[AssayDescription] OR \"fluorescent\"[AssayDescription]) NOT \"Summary\"[ActivityOutcomeMethod]" };
	private static String[] searchNames = { PCPromiscuityFactory.mlpAssaysName, PCPromiscuityFactory.chemblName,
			PCPromiscuityFactory.betaLactamaseName, PCPromiscuityFactory.luciferaseName, PCPromiscuityFactory.fluorescentName };

	public PCPromiscuityParameters params;

	private static final Logger log = LoggerFactory.getLogger(PCPromiscuityMain.class);

	public PCPromiscuityMain(PCPromiscuityParameters uParams) {
		this.params = uParams;
	}

	public Map<Long, CompoundPromiscuityInfo> getCompoundPromiscuityInfoMap() throws Exception {
		OverallListsAndMaps overall = setOverallVariables();
		Map<Long, CompoundPromiscuityInfo> compoundMap = setCompoundsPromiscuityInfo(overall);
		return compoundMap;
	}

	private Map<Long, CompoundPromiscuityInfo> setCompoundsPromiscuityInfo(OverallListsAndMaps overall) throws Exception {
		PCPromiscuityFactory promFactory = new PCPromiscuityFactory();
		Map<Long, CompoundPromiscuityInfo> compoundMap = promFactory.getCompoundsWithDescriptors(params.getIds(), params.getDatabase());
		new FunctionalGroupDetectionFactory().calculateFunctionalGroups(compoundMap);

		for (Long keyId : compoundMap.keySet()) {

			log.info("Setting counts for compound: " + keyId);

			CompoundPromiscuityInfo compound = compoundMap.get(keyId);
			if (compound.getOnHold())
				continue;

			if (params.getDatabase().equalsIgnoreCase("pccompound")) {
				compound.setCID(keyId);
			} else {
				ELinkResult result = (ELinkResult) overall.getSIDToCIDMap().get(keyId);
				List<Long> CIDs = result.getIds("pccompound", "pcsubstance_pccompound_same");
				compound.setCID(CIDs.get(0));
			}

			promFactory.addRuleOfFiveViolations(compound);
			Map<String, PromiscuityCount<?>> countMap = compound.getCounts();
			promFactory.addAllAssayCount(keyId, overall.getCompoundToAIDsMap(), params.getDatabase(), countMap);

			if (!params.getSimpleMode() || params.getPerProteinMode()) {

				promFactory.addAllProteinCount(overall, countMap);
				promFactory.addAllNoProteinAIDCount(countMap, overall);

				if (!params.getSimpleMode())
					promFactory.addProjectCountsPerCompound(overall, countMap);

				if (!params.getPerProteinMode()) {
					promFactory.addAdvancedCounts(overall, countMap);
				} else {
					Map<Protein, Map<String, PromiscuityCount<?>>> perProteinCountMap = promFactory.allAssayCountPerProtein(countMap,
							overall.getProteinAIDMap());

					Map<String, PromiscuityCount<?>> noProteinCountMap = new HashMap<String, PromiscuityCount<?>>();
					PromiscuityCount<Long> allAssayNoProteinCount = (PromiscuityCount<Long>) countMap
							.get(PCPromiscuityFactory.noProteinsName);
					noProteinCountMap.put(PCPromiscuityFactory.allAssayName, allAssayNoProteinCount);
					noProteinCountMap.put(PCPromiscuityFactory.noProteinsName, allAssayNoProteinCount);

					if (!params.getSimpleMode()) {
						for (Protein keyProtein : perProteinCountMap.keySet()) {
							Map<String, PromiscuityCount<?>> countMapPerProtein = perProteinCountMap.get(keyProtein);
							promFactory.addAdvancedCountsPerProtein(overall, countMapPerProtein, countMap);
							promFactory.addAllNoProteinAIDCount(countMapPerProtein, overall);
							perProteinCountMap.put(keyProtein, countMapPerProtein);
						}
						promFactory.addAdvancedCountsPerProtein(overall, noProteinCountMap, countMap);
						promFactory.addAllNoProteinAIDCount(noProteinCountMap, overall);
					}
					compound.setPerProteinCounts(perProteinCountMap);
					compound.setNoProteinCounts(noProteinCountMap);
				}
			}

			compound.setCounts(countMap);
			compoundMap.put(compound.getId(), compound);
		}
		log.info("Memory usage after compound map completely set up: " + promFactory.memUsage());
		return compoundMap;
	}

	private OverallListsAndMaps setOverallVariables() throws Exception {
		OverallListsAndMaps overall = new OverallListsAndMaps();
		String db = params.getDatabase();

		List<String> linkNames = Arrays.asList(new String[] { db + "_pcassay", db + "_pcassay_active" });
		ELinkWebSession session = ELinkWebSession.newInstance(db, "pcassay", linkNames, params.getIds(), "");
		log.info("Number of ids in compound to pcassay link request: " + String.valueOf(params.getIds().size()));
		session.run();
		overall.setCompoundToAIDsMap(session.getELinkResultsAsMap());

		OverallListsAndMapsFactory overallFactory = new OverallListsAndMapsFactory();

		if (params.getPerProteinMode() || !params.getSimpleMode()) {
			overall.setAllAIDs(session.getAllIds(db + "_pcassay"));
			log.info("Number of All AIDs: " + overall.getAllAIDs().size());

			String shortDB = db.substring(2, db.length());
			List<Long> proteinAssays = overallFactory.aidListEsearch(params.getIds(), "pcassay_protein_target[Filter]", shortDB);
			overall.setAllProteinAIDs(proteinAssays);
			overall.setAidProteinMap(overallFactory.getAIDProteinMap(new ArrayList<Long>(proteinAssays)));

			List<Long> noProteinAIDs = (List<Long>) CollectionUtils.subtract(overall.getAllAIDs(), proteinAssays);
			overall.setAllNoProteinAIDs(noProteinAIDs);

			log.info("Number of aids returned from eSummary request: " + overall.getAidProteinMap().keySet().size());

		}

		if (params.getPerProteinMode()) {
			overall.setAllProteins(overallFactory.allProteinSet(overall.getAidProteinMap()));
			overall.setProteinAIDMap(overallFactory.proteinAIDMap(overall.getAidProteinMap(), overall.getAllProteins()));
		}

		if (!params.getSimpleMode()) {
			// all aids in each desired assay count category for advanced mode
			Map<String, List<Long>> overallTotalAIDMap = new HashMap<String, List<Long>>(searchNames.length);
			String shortDB = db.substring(2, db.length());
			for (int ii = 0; ii < searchTerms.length; ii++) {
				String searchTerm = searchTerms[ii];
				log.info("Advanced search: " + searchTerm);
				List<Long> list = overallFactory.aidListEsearch(params.getIds(), searchTerm, shortDB);
				overallTotalAIDMap.put(searchNames[ii], list);
			}
			overall.setAdvancedCountTotalAIDMap(overallTotalAIDMap);

			// eLink request for summary aids related to not ChEMBL aids
			String linkNeighbor = "pcassay_pcassay_neighbor_list";

			log.info("Number of ChEMBL AIDs: " + overall.getAdvancedCountTotalAIDMap().get(PCPromiscuityFactory.chemblName).size());

			List<Long> notChEMBLAIDs = (List<Long>) CollectionUtils.subtract(overall.getAllAIDs(), overall.getAdvancedCountTotalAIDMap()
					.get(PCPromiscuityFactory.chemblName));
			session = ELinkWebSession.newInstance("pcassay", "pcassay", Arrays.asList(new String[] { linkNeighbor }), notChEMBLAIDs,
					"summary[activityoutcomemethod]");

			log.info("Number of ids in link request: " + String.valueOf(notChEMBLAIDs.size()));
			session.run();
			overall.setAllSummaries(session.getAllIds(linkNeighbor));
			overall.setSummaryProteinMap(overallFactory.getAIDProteinMap(new ArrayList<Long>(overall.getAllSummaries())));
			Map<Long, List<ELinkResult>> aidToSummaryMap = session.getELinkResultsAsMap();
			overall.setSummaryToAIDsMap(overallFactory.getSummaryToAIDsMap(aidToSummaryMap));
			overall.setMlpSummaries(overallFactory.aidtoAIDListEsearch(new ArrayList<Long>(overall.getAllSummaries()), searchTerms[0]));
		}

		if (db.equalsIgnoreCase("pcsubstance")) {
			session = ELinkWebSession.newInstance(db, "pccompound", Arrays.asList(new String[] { "pcsubstance_pccompound_same" }),
					params.getIds(), "");
			session.run();
			overall.setSIDToCIDMap(session.getELinkResultsAsMap());
		}

		log.info("Set up all overall lists");
		return overall;
	}

}
