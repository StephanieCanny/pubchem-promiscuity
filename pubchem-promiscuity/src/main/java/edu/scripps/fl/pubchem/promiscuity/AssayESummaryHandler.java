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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.scripps.fl.pubchem.promiscuity.model.Protein;

public class AssayESummaryHandler extends DefaultHandler {

	private StringBuffer sb;
	private Map<Long, List<Protein>> map = new HashMap<Long, List<Protein>>();
	private Long uid;
	private List<Protein> list;
	private Protein protein;
	private boolean isProtein;

	public Map<Long, List<Protein>> getMap() {
		return map;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		sb = new StringBuffer();
		if (qName.equalsIgnoreCase("DocumentSummary")) {
			uid = Long.parseLong(attributes.getValue("uid"));
			list = new ArrayList<Protein>();
		}
		if (qName.equalsIgnoreCase("ProteinTarget")) {
			protein = new Protein();
			isProtein = true;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("DocumentSummary")) {
			if (list.size() > 0)
				map.put(uid, list);
		} else if ("ProteinTarget".equals(qName)) {
			list.add(protein);
			isProtein = false;
		} else if ("GI".equals(qName))
			protein.setGi(Long.parseLong(sb.toString()));
		else if (isProtein && "Name".equals(qName))
			protein.setName(sb.toString());
	}

	public void characters(char ch[], int start, int length) throws SAXException {
		sb.append(ch, start, length);
	}
}