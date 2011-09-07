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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.scripps.fl.pubchem.promiscuity.model.CompoundPromiscuityInfo;

public class CompoundESummaryHandler extends DefaultHandler {

	private static String cSmiles = "CanonicalSmiles", mWeight = "MolecularWeight", xLogP = "XLogP", hBondDonor = "HydrogenBondDonorCount",
			hBondAcceptor = "HydrogenBondAcceptorCount";

	private static Set<String> descriptors = new HashSet<String>(Arrays.asList(new String[] { "MinAC", "MaxAC", "MinTC", "MaxTC", cSmiles,
			mWeight, xLogP, hBondDonor, hBondAcceptor, "TPSA", "Complexity", "RotatableBondCount", "MolecularFormula", "TotalFormalCharge",
			"HeavyAtomCount", "AtomChiralCount", "AtomChiralDefCount", "AtomChiralUndefCount", "BondChiralCount", "BondChiralDefCount",
			"BondChiralUndefCount", "IsotopeAtomCount", "CovalentUnitCount", "TautomerCount" }));

	private Map<Long, CompoundPromiscuityInfo> map = new HashMap<Long, CompoundPromiscuityInfo>();
	private CompoundPromiscuityInfo cpi = null;
	private int depth = 0;
	private StringBuffer sb;

	public Map<Long, CompoundPromiscuityInfo> getCompoundIdMap() {
		return map;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		sb = new StringBuffer();
		depth++;
		if (qName.equalsIgnoreCase("DocumentSummary")) {
			cpi = new CompoundPromiscuityInfo();
			cpi.setId(Long.parseLong(attributes.getValue("uid")));
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("DocumentSummary"))
			map.put(cpi.getId(), cpi);
		else if ("HoldUntilDate".equals(qName)) {
			Date holdDate = getDate("yyyy/MM/dd", sb.toString());
			if ((new Date()).compareTo(holdDate) < 0)
				cpi.setOnHold(true);
		} else if (depth == 4 && descriptors.contains(qName))
			cpi.getDescriptors().put(qName, sb.toString());
		depth--;
	}

	private Date getDate(String format, String text) {
		try {
			DateFormat dateFormat = new SimpleDateFormat(format);
			Date date = dateFormat.parse(text);
			return date;
		} catch (Exception ex) {
			return null;
		}
	}

	public void characters(char ch[], int start, int length) throws SAXException {
		sb.append(ch, start, length);
	}
}