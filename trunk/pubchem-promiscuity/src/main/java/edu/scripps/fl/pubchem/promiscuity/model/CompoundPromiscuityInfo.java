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
import java.util.List;
import java.util.Map;

public class CompoundPromiscuityInfo {

	private Long id;
	private Long CID;
	private String database;
	private Boolean onHold = false;
	private boolean possibleFalseAromaticityDetection = false;
	private Map<String, Object> descriptors = new HashMap<String, Object>();
	private Map<String, CategorizedFunctionalGroups> categorizedFunctionalGroupsMap = new HashMap<String, CategorizedFunctionalGroups>();
	private Map<String, PromiscuityCount<?>> counts = new HashMap<String, PromiscuityCount<?>>();
	private Map<Protein, Map<String, PromiscuityCount<?>>> perProteinCounts = new HashMap<Protein, Map<String, PromiscuityCount<?>>>();
	private Map<String, PromiscuityCount<?>> noProteinCounts = new HashMap<String, PromiscuityCount<?>>();

	public Map<String, PromiscuityCount<?>> getCounts() {
		return counts;
	}

	public String getDatabase() {
		return database;
	}

	public Map<String, Object> getDescriptors() {
		return descriptors;
	}

	public Long getId() {
		return id;
	}

	public Boolean getOnHold() {
		return onHold;
	}

	public Map<Protein, Map<String, PromiscuityCount<?>>> getPerProteinCounts() {
		return perProteinCounts;
	}

	public void setCounts(Map<String, PromiscuityCount<?>> counts) {
		this.counts = counts;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setDescriptors(Map<String, Object> descriptors) {
		this.descriptors = descriptors;
	}

	public Map<String, CategorizedFunctionalGroups> getCategorizedFunctionalGroupsMap() {
		return categorizedFunctionalGroupsMap;
	}

	public void setCategorizedFunctionalGroupsMap(Map<String, CategorizedFunctionalGroups> categorizedFunctionalGroupsMap) {
		this.categorizedFunctionalGroupsMap = categorizedFunctionalGroupsMap;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setOnHold(Boolean onHold) {
		this.onHold = onHold;
	}

	public void setPerProteinCounts(Map<Protein, Map<String, PromiscuityCount<?>>> perProteinCounts) {
		this.perProteinCounts = perProteinCounts;
	}

	public void setNoProteinCounts(Map<String, PromiscuityCount<?>> noProteinCounts) {
		this.noProteinCounts = noProteinCounts;
	}

	public Map<String, PromiscuityCount<?>> getNoProteinCounts() {
		return noProteinCounts;
	}

	public void setCID(Long cID) {
		CID = cID;
	}

	public Long getCID() {
		return CID;
	}

	public boolean isPossibleFalseAromaticityDetection() {
		return possibleFalseAromaticityDetection;
	}

	public void setPossibleFalseAromaticityDetection(boolean possibleFalseAromaticityDetection) {
		this.possibleFalseAromaticityDetection = possibleFalseAromaticityDetection;
	}

}
