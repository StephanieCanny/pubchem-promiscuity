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
import java.util.List;

public class PromiscuityCount<E> {

	private List<E> active = new ArrayList<E>();
	private List<E> total = new ArrayList<E>();
	private String name;

	public PromiscuityCount(String name, List<E> active, List<E> total) {
		setName(name);
		setActive(active);
		setTotal(total);
	}

	public List<E> getActive() {
		return active;
	}

	public void setActive(List<E> active) {
		this.active = active;
	}

	public List<E> getTotal() {
		return total;
	}

	public void setTotal(List<E> total) {
		this.total = total;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}