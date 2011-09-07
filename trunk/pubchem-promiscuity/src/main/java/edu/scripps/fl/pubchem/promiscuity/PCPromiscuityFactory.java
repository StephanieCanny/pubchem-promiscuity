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
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.promiscuity.model.CompoundPromiscuityInfo;
import edu.scripps.fl.pubchem.promiscuity.model.OverallListsAndMaps;
import edu.scripps.fl.pubchem.promiscuity.model.PromiscuityCount;
import edu.scripps.fl.pubchem.promiscuity.model.Protein;
import edu.scripps.fl.pubchem.web.ELinkResult;
import edu.scripps.fl.pubchem.web.entrez.EUtilsFactory;

public class PCPromiscuityFactory {

	protected static final String cSmiles = "CanonicalSmiles", mWeight = "MolecularWeight", xLogP = "XLogP",
			hBondDonor = "HydrogenBondDonorCount", hBondAcceptor = "HydrogenBondAcceptorCount", ruleFiveName = "Rule Of 5 Violations";

	protected static final String allProteinsName = "AllProteins", noProteinsName = "NoProteinAssays", allAssayName = "AllAssays",
			allProjectsName = "AllProjects", betaLactamaseName = "BetaLactamaseAssays", luciferaseName = "LuciferaseAssays",
			chemblName = "ChEMBLAssays", mlpAssaysName = "MLPAssays", mlpProjectsNames = "MLPProjects",
			fluorescentName = "FluorescenceAssays";

	private static final String[] ruleOfFive = { mWeight, hBondDonor, hBondAcceptor, xLogP };

	private static final Logger log = LoggerFactory.getLogger(PCPromiscuityFactory.class);

	/**
	 * advanced counts for per Compound mode(i.e. MLP assays,ChEMBL assays, Beta
	 * Lactamase assays, Luciferase assays, Projects, MLP projects)
	 */
	@SuppressWarnings("unchecked")
	private void addAdvancedAssayCounts(OverallListsAndMaps overall, Map<String, PromiscuityCount<?>> countMap) {
		PromiscuityCount<Long> allAssayCount = (PromiscuityCount<Long>) countMap.get(PCPromiscuityFactory.allAssayName);
		if (allAssayCount != null) {
			List<Long> allAssayTotal = allAssayCount.getTotal();
			List<Long> allAssayActive = allAssayCount.getActive();

			Map<String, List<Long>> advancedCountTotalMap = overall.getAdvancedCountTotalAIDMap();
			for (String searchName : advancedCountTotalMap.keySet()) {
				List<Long> aidList = advancedCountTotalMap.get(searchName);
				List<Long> compoundAIDTotalList = (List<Long>) CollectionUtils.intersection(allAssayTotal, aidList);
				List<Long> compoundAIDActiveList = (List<Long>) CollectionUtils.intersection(allAssayActive, aidList);
				PromiscuityCount<Long> count = new PromiscuityCount<Long>(searchName, compoundAIDActiveList, compoundAIDTotalList);
				countMap.put(count.getName(), count);
			}
		}
	}

	public void addAdvancedCounts(OverallListsAndMaps overall, Map<String, PromiscuityCount<?>> countMap) {
		log.info("\t adding advanced assay counts");
		addAdvancedAssayCounts(overall, countMap);
		addMLPProjectCounts(overall, countMap);
		log.info("\t advance assay counts complete");
	}

	public void addAdvancedCountsPerProtein(OverallListsAndMaps overall, Map<String, PromiscuityCount<?>> countMapPerProtein,
			Map<String, PromiscuityCount<?>> countMapPerCompound) {
		addAllProteinCount(overall, countMapPerProtein);
		addProjectCountsPerCompoundAndProtein(overall, countMapPerProtein, countMapPerCompound);
		addAdvancedCounts(overall, countMapPerProtein);
	}

	public void addAllAssayCount(Long id, Map<Long, List<ELinkResult>> elinkResults, String db, Map<String, PromiscuityCount<?>> countMap) {

		ELinkResult result = (ELinkResult) elinkResults.get(id);

		List<Long> actives = new ArrayList<Long>();
		List<Long> all = new ArrayList<Long>();
		if (result != null) {
			if (null != result.getIds("pcassay", db + "_pcassay_active"))
				actives = result.getIds("pcassay", db + "_pcassay_active");
			if (null != result.getIds("pcassay", db + "_pcassay"))
				all = result.getIds("pcassay", db + "_pcassay");
		}
		PromiscuityCount<Long> allAssayCount = new PromiscuityCount<Long>(allAssayName, actives, all);
		countMap.put(allAssayName, allAssayCount);

		log.info("\t all assay count complete: active: " + actives.size() + " total: " + all.size());
	}

	public void addAllProteinCount(OverallListsAndMaps overall, Map<String, PromiscuityCount<?>> counts) {

		log.info("\t adding all protein and no protein counts");

		PromiscuityCount<Long> allAssays = (PromiscuityCount<Long>) counts.get(allAssayName);

		Set<Protein> activeProteins = new HashSet<Protein>();
		Set<Protein> allProteins = new HashSet<Protein>();

		List<Long> allProteinAIDs = overall.getAllProteinAIDs();
		List<Long> activeProteinAIDs = (List<Long>) CollectionUtils.intersection(allAssays.getActive(), allProteinAIDs);
		List<Long> totalProteinAIDs = (List<Long>) CollectionUtils.intersection(allAssays.getTotal(), allProteinAIDs);
		for (Long aid : totalProteinAIDs) {
			List<Protein> proteins = overall.getAidProteinMap().get(aid);
			if (proteins != null && proteins.size() > 0) {
				allProteins.addAll(proteins);
				if (activeProteinAIDs.contains(aid))
					activeProteins.addAll(proteins);
			}

		}
		PromiscuityCount<Protein> allProteinsCount = new PromiscuityCount<Protein>(allProteinsName, new ArrayList<Protein>(activeProteins),
				new ArrayList<Protein>(allProteins));
		counts.put(allProteinsCount.getName(), allProteinsCount);

		log.info("\t all protein counts complete");
	}

	public void addAllNoProteinAIDCount(Map<String, PromiscuityCount<?>> countMap, OverallListsAndMaps overall) {
		PromiscuityCount<Long> allAssayCount = (PromiscuityCount<Long>) countMap.get(PCPromiscuityFactory.allAssayName);
		List<Long> actives = allAssayCount.getActive();
		List<Long> total = allAssayCount.getTotal();
		List<Long> allNoProteinAIDs = overall.getAllNoProteinAIDs();
		List<Long> activeNoProtein = (List<Long>) CollectionUtils.intersection(allNoProteinAIDs, actives);
		List<Long> totalNoProtein = (List<Long>) CollectionUtils.intersection(allNoProteinAIDs, total);
		PromiscuityCount<Long> noProteinCount = new PromiscuityCount<Long>(noProteinsName, activeNoProtein, totalNoProtein);
		countMap.put(noProteinsName, noProteinCount);
	}

	@SuppressWarnings("unchecked")
	private void addMLPProjectCounts(OverallListsAndMaps overall, Map<String, PromiscuityCount<?>> countMap) {
		PromiscuityCount<Long> summaryCount = (PromiscuityCount<Long>) countMap.get(allProjectsName);
		if (summaryCount != null) {
			List<Long> mlpSummaryTotal = (List<Long>) CollectionUtils.intersection(summaryCount.getTotal(), overall.getMlpSummaries());
			List<Long> mlpSummaryActive = (List<Long>) CollectionUtils.intersection(summaryCount.getActive(), overall.getMlpSummaries());
			PromiscuityCount<Long> mlpSummaryCount = new PromiscuityCount<Long>(mlpProjectsNames, mlpSummaryActive, mlpSummaryTotal);
			countMap.put(mlpSummaryCount.getName(), mlpSummaryCount);
		}
	}

	@SuppressWarnings("unchecked")
	public void addProjectCountsPerCompound(OverallListsAndMaps overall, Map<String, PromiscuityCount<?>> countMap) {
		log.info("\t adding project counts");

		PromiscuityCount<Long> allAssayCount = (PromiscuityCount<Long>) countMap.get(allAssayName);
		if (allAssayCount != null) {
			List<Long> allAssayTotal = (ArrayList<Long>) allAssayCount.getTotal();
			List<Long> allAssayActive = allAssayCount.getActive();
			PromiscuityCount<Long> summaryCount = getSummaryCountPerCompound(overall.getAllSummaries(), overall.getSummaryToAIDsMap(),
					allAssayTotal, allAssayActive, overall.getAidProteinMap(), overall.getSummaryProteinMap());
			countMap.put(summaryCount.getName(), summaryCount);
		}

		log.info("\t project counts complete");

	}

	@SuppressWarnings("unchecked")
	private void addProjectCountsPerCompoundAndProtein(OverallListsAndMaps overall, Map<String, PromiscuityCount<?>> countMapPerProtein,
			Map<String, PromiscuityCount<?>> countMapPerCompound) {
		PromiscuityCount<Long> allAssayCount = (PromiscuityCount<Long>) countMapPerProtein.get(allAssayName);
		if (allAssayCount != null) {
			PromiscuityCount<Long> allSummariesPerCompound = (PromiscuityCount<Long>) countMapPerCompound.get(allProjectsName);
			if (allSummariesPerCompound != null) {
				List<Long> totalSummary = getTotalSummaryListPerCompoundAndProtein(allSummariesPerCompound.getTotal(),
						overall.getSummaryToAIDsMap(), allAssayCount.getTotal());

				List<Long> activeSummary = new ArrayList<Long>();

				activeSummary = (List<Long>) CollectionUtils.intersection(allSummariesPerCompound.getActive(), totalSummary);
				PromiscuityCount<Long> count = new PromiscuityCount<Long>(allProjectsName, activeSummary, totalSummary);
				countMapPerProtein.put(allProjectsName, count);
			}
		}
	}

	public void addRuleOfFiveViolations(CompoundPromiscuityInfo compound) {
		Map<String, Object> descriptors = compound.getDescriptors();
		Integer violations = 0;
		Integer[] ruleOfFiveVals = new Integer[] { 500, 5, 10, 5 };

		for (int ii = 0; ii <= 3; ii++) {
			String oo = (String) descriptors.get(ruleOfFive[ii]);
			if (oo != null && !oo.equals("")) {
				Double dd = Double.parseDouble(oo);
				if (dd > ruleOfFiveVals[ii])
					violations = violations + 1;
			}
		}
		descriptors.put(ruleFiveName, violations);
		compound.setDescriptors(descriptors);
	}

	@SuppressWarnings("unchecked")
	public Map<Protein, Map<String, PromiscuityCount<?>>> allAssayCountPerProtein(Map<String, PromiscuityCount<?>> countMap,
			Map<Protein, List<Long>> proteinAIDMap) {
		Map<Protein, Map<String, PromiscuityCount<?>>> perProteinPromiscuityCountMap = new HashMap<Protein, Map<String, PromiscuityCount<?>>>();

		PromiscuityCount<Protein> proteinCount = (PromiscuityCount<Protein>) countMap.get(allProteinsName);
		if (proteinCount != null) {
			List<Protein> totalProteins = proteinCount.getTotal();
			PromiscuityCount<Long> allAssayCount = (PromiscuityCount<Long>) countMap.get(allAssayName);
			if (allAssayCount != null) {
				List<Long> allAssays = allAssayCount.getTotal();
				List<Long> activeAssays = allAssayCount.getActive();

				for (Protein protein : totalProteins) {
					List<Long> activeAIDsPerProtein = new ArrayList<Long>();
					List<Long> totalAIDsPerProtein = new ArrayList<Long>();
					Map<String, PromiscuityCount<?>> promiscuityCountMap = new HashMap<String, PromiscuityCount<?>>();
					List<Long> aids = proteinAIDMap.get(protein);
					activeAIDsPerProtein = (List<Long>) CollectionUtils.intersection(activeAssays, aids);
					totalAIDsPerProtein = (List<Long>) CollectionUtils.intersection(allAssays, aids);
					PromiscuityCount<Long> count = new PromiscuityCount<Long>(allAssayName, activeAIDsPerProtein, totalAIDsPerProtein);
					promiscuityCountMap.put(count.getName(), count);
					perProteinPromiscuityCountMap.put(protein, promiscuityCountMap);
				}
			}
		}
		return perProteinPromiscuityCountMap;
	}

	private void checkIfActiveSummary(List<Long> xrefAIDs, List<Long> allAssayActive, Long summaryAID, List<Long> summaryActive,
			Map<Long, List<Protein>> aidProteinMap, Map<Long, List<Protein>> summaryProteinMap) {

		List<Long> activeXrefAIDs = (List<Long>) CollectionUtils.intersection(xrefAIDs, allAssayActive);
		if (activeXrefAIDs.size() > 0) {
			Boolean isActive = true;
			int ii = 0;
			List<Protein> summaryProteins = summaryProteinMap.get(summaryAID);

			while (isActive && ii < activeXrefAIDs.size()) {
				Long activeXrefAID = activeXrefAIDs.get(ii);
				List<Protein> xrefProteins = aidProteinMap.get(activeXrefAID);
				if (summaryProteins != null) {
					if (xrefProteins != null) {
						if (!summaryProteins.containsAll(xrefProteins))
							isActive = false;
						else if (summaryProteins.size() > 0 && xrefProteins.size() == 0) {
							isActive = false;
						} else
							ii = ii + 1;
					} else if (xrefProteins == null && summaryProteins.size() > 0)
						isActive = false;
					else
						ii = ii + 1;
				} else {
					if (xrefProteins != null && xrefProteins.size() > 0)
						isActive = false;
					else
						ii = ii + 1;
				}

			}

			if (isActive)
				summaryActive.add(summaryAID);
		}

	}

	public Map<Long, CompoundPromiscuityInfo> getCompoundsWithDescriptors(List<Long> ids, String db) throws Exception {
		log.info("Number of compounds in eSummary request: " + ids.size());
		log.info("Memory usage before getting compound eSummary document: " + memUsage());

		InputStream is = EUtilsFactory.getInstance().getSummaries(ids, db);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		CompoundESummaryHandler handler = new CompoundESummaryHandler();
		saxParser.parse(is, handler);

		log.info("Memory usage after getting compound eSummary document: " + memUsage());

		return handler.getCompoundIdMap();
	}

	private PromiscuityCount<Long> getSummaryCountPerCompound(Set<Long> allSummaries, Map<Long, List<Long>> summaryToAIDsMap,
			List<Long> allAssayTotal, List<Long> allAssayActive, Map<Long, List<Protein>> aidProteinMap,
			Map<Long, List<Protein>> summaryProteinMap) {
		List<Long> summaryTotal = new ArrayList<Long>();
		List<Long> summaryActive = new ArrayList<Long>();
		for (Entry<Long, List<Long>> entry : summaryToAIDsMap.entrySet()) {
			List<Long> xrefAIDs = entry.getValue();
			if (CollectionUtils.containsAny(xrefAIDs, allAssayTotal)) {
				summaryTotal.add(entry.getKey());
				checkIfActiveSummary(xrefAIDs, allAssayActive, entry.getKey(), summaryActive, aidProteinMap, summaryProteinMap);
			}
		}
		PromiscuityCount<Long> summaryCount = new PromiscuityCount<Long>(allProjectsName, summaryActive, summaryTotal);
		return summaryCount;
	}

	public List<Long> getTotalSummaryListPerCompoundAndProtein(List<Long> compoundSummaries, Map<Long, List<Long>> summaryToAIDsMap,
			List<Long> allAssayTotal) {
		List<Long> summaryTotal = new ArrayList<Long>();
		for (Long summaryAID : compoundSummaries) {
			List<Long> xrefAIDs = summaryToAIDsMap.get(summaryAID);
			if (CollectionUtils.intersection(xrefAIDs, allAssayTotal).size() > 0)
				summaryTotal.add(summaryAID);
		}
		return summaryTotal;
	}

	public long memUsage() {
		long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		return mem / 1024 / 1024;
	}

}
