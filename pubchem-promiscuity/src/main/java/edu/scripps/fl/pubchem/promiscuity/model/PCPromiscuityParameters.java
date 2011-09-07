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

import java.util.List;

public class PCPromiscuityParameters {

	private String database;
	private Boolean simpleMode;
	private Boolean perProteinMode;
	private List<Long> ids;

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public Boolean getSimpleMode() {
		return simpleMode;
	}

	public void setSimpleMode(Boolean simpleMode) {
		this.simpleMode = simpleMode;
	}

	public Boolean getPerProteinMode() {
		return perProteinMode;
	}

	public void setPerProteinMode(Boolean perProteinMode) {
		this.perProteinMode = perProteinMode;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public List<Long> getIds() {
		return ids;
	}

}
