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
package edu.scripps.fl.pubchem.promiscuity.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.scripps.fl.pubchem.web.ELinkResult;

public class OverallListsAndMaps {

	/*
	 * allAIDs is the set of all the AIDs that all of the compounds have been
	 * tested in. allSummaries is the set of all the Summary AIDs that the set
	 * of all the AIDs are related to.
	 */
	private Set<Long> allAIDs = new HashSet<Long>(), allSummaries = new HashSet<Long>();
	/*
	 * aidProteinMap is a map of all AIDs to their target proteins.
	 * summaryProteinMap is of all Summaries to their target proteins.
	 */
	private Map<Long, List<Protein>> aidProteinMap = new HashMap<Long, List<Protein>>(),
			summaryProteinMap = new HashMap<Long, List<Protein>>();
	/*
	 * mlpSummaries is a sublist of allSummaries where the source category for
	 * the Summary assay is NIH Molecular Libraries Program
	 */
	private List<Long> mlpSummaries = new ArrayList<Long>(), allProteinAIDs = new ArrayList<Long>(),
			allNoProteinAIDs = new ArrayList<Long>();
	private Map<Long, List<Long>> summaryToAIDsMap = new HashMap<Long, List<Long>>();
	private Map<String, List<Long>> advancedCountTotalAIDMap = new HashMap<String, List<Long>>();
	private Map<Long, List<ELinkResult>> compoundToAIDsMap = new HashMap<Long, List<ELinkResult>>();
	private Map<Long, List<ELinkResult>> SIDToCIDMap = new HashMap<Long, List<ELinkResult>>();
	private Set<Protein> allProteins = new HashSet<Protein>();
	private Map<Protein, List<Long>> proteinAIDMap = new HashMap<Protein, List<Long>>();

	public Map<String, List<Long>> getAdvancedCountTotalAIDMap() {
		return advancedCountTotalAIDMap;
	}

	public Map<Long, List<Protein>> getAidProteinMap() {
		return aidProteinMap;
	}

	public Set<Long> getAllAIDs() {
		return allAIDs;
	}

	public Set<Protein> getAllProteins() {
		return allProteins;
	}

	public Set<Long> getAllSummaries() {
		return allSummaries;
	}

	public Map<Long, List<ELinkResult>> getCompoundToAIDsMap() {
		return compoundToAIDsMap;
	}

	public List<Long> getMlpSummaries() {
		return mlpSummaries;
	}

	public Map<Protein, List<Long>> getProteinAIDMap() {
		return proteinAIDMap;
	}

	public Map<Long, List<ELinkResult>> getSIDToCIDMap() {
		return SIDToCIDMap;
	}

	public Map<Long, List<Protein>> getSummaryProteinMap() {
		return summaryProteinMap;
	}

	public Map<Long, List<Long>> getSummaryToAIDsMap() {
		return summaryToAIDsMap;
	}

	public void setAdvancedCountTotalAIDMap(Map<String, List<Long>> advancedCountTotalAIDMap) {
		this.advancedCountTotalAIDMap = advancedCountTotalAIDMap;
	}

	public void setAidProteinMap(Map<Long, List<Protein>> aidProteinMap) {
		this.aidProteinMap = aidProteinMap;
	}

	public void setAllAIDs(Set<Long> allAIDs) {
		this.allAIDs = allAIDs;
	}

	public void setAllProteins(Set<Protein> allProteins) {
		this.allProteins = allProteins;
	}

	public void setAllSummaries(Set<Long> allSummaries) {
		this.allSummaries = allSummaries;
	}

	public void setCompoundToAIDsMap(Map<Long, List<ELinkResult>> compoundToAIDsMap) {
		this.compoundToAIDsMap = compoundToAIDsMap;
	}

	public void setMlpSummaries(List<Long> mlpSummaries) {
		this.mlpSummaries = mlpSummaries;
	}

	public void setProteinAIDMap(Map<Protein, List<Long>> proteinAIDMap) {
		this.proteinAIDMap = proteinAIDMap;
	}

	public void setSIDToCIDMap(Map<Long, List<ELinkResult>> sIDToCIDMap) {
		SIDToCIDMap = sIDToCIDMap;
	}

	public void setSummaryProteinMap(Map<Long, List<Protein>> summaryProteinMap) {
		this.summaryProteinMap = summaryProteinMap;
	}

	public void setSummaryToAIDsMap(Map<Long, List<Long>> summaryToAIDsMap) {
		this.summaryToAIDsMap = summaryToAIDsMap;
	}

	public void setAllProteinAIDs(List<Long> allProteinAIDs) {
		this.allProteinAIDs = allProteinAIDs;
	}

	public List<Long> getAllProteinAIDs() {
		return allProteinAIDs;
	}

	public void setAllNoProteinAIDs(List<Long> allNoProteinAIDs) {
		this.allNoProteinAIDs = allNoProteinAIDs;
	}

	public List<Long> getAllNoProteinAIDs() {
		return allNoProteinAIDs;
	}

}
