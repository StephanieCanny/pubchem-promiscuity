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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import edu.scripps.fl.match.JoeLibMatcher;
import edu.scripps.fl.match.SMARTSMatcher;
import edu.scripps.fl.pubchem.promiscuity.model.CategorizedFunctionalGroups;
import edu.scripps.fl.pubchem.promiscuity.model.CompoundPromiscuityInfo;
import edu.scripps.fl.pubchem.promiscuity.model.FunctionalGroup;

public class FunctionalGroupDetectionFactory {

	private static final Logger log = LoggerFactory.getLogger(FunctionalGroupDetectionFactory.class);

	public FunctionalGroupDetectionFactory() {

	}

	public void calculateFunctionalGroups(Map<Long, CompoundPromiscuityInfo> compounds) throws IOException,
			ParserConfigurationException, SAXException, URISyntaxException {
		List<CategorizedFunctionalGroups> catGroups = GetFunctionalGroups();
		
		for (Long id : compounds.keySet()) {
			CompoundPromiscuityInfo compound = compounds.get(id);
			if (!compound.getOnHold()) {
				Map<String, CategorizedFunctionalGroups> categorizedCompoundGroupsMap = new HashMap<String, CategorizedFunctionalGroups>();
				String smiles = (String) compound.getDescriptors().get(PCPromiscuityFactory.cSmiles);
				log.info(id + "\t" + smiles);
				if (smiles != null && !smiles.equals("")) {
					SMARTSMatcher matcher = new JoeLibMatcher();
					matcher.setTarget(smiles);
					for (CategorizedFunctionalGroups catGroup : catGroups) {
						CategorizedFunctionalGroups compoundCategGroups = new CategorizedFunctionalGroups();
						List<FunctionalGroup> compoundGroups = new ArrayList<FunctionalGroup>();
						for (FunctionalGroup group : catGroup.getFunctionalGroups()) {
							if (matcher.matches(group.getSMARTS()))
								compoundGroups.add(group);
						}
						compoundCategGroups.setFunctionalGroups(compoundGroups);
						compoundCategGroups.setCategory(catGroup.getCategory());
						categorizedCompoundGroupsMap.put(catGroup.getCategory(), compoundCategGroups);
					}
					compound.setPossibleFalseAromaticityDetection(matcher.checkAromaticityDetection());
				}
				compound.setCategorizedFunctionalGroupsMap(categorizedCompoundGroupsMap);
			}
			
			compounds.put(id, compound);
		}
	}

	protected List<CategorizedFunctionalGroups> GetFunctionalGroups() throws ParserConfigurationException, SAXException, IOException,
			URISyntaxException {
		List<CategorizedFunctionalGroups> categorizedGroups = new ArrayList<CategorizedFunctionalGroups>();

		String fileName = System.getProperty("user.home") + "\\FunctionalGroups_V2.xml";
		File functionalGroupsXML = new File(fileName);
		Document doc;
		log.info("Looking for file: " + fileName);
		if (functionalGroupsXML.exists()) {
			log.info("Found file");
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			org.w3c.dom.Document doc2 = builder.parse(functionalGroupsXML);
			DOMReader reader = new DOMReader();
			doc = reader.read(doc2);
		} else {
			log.info("Did not find file");
			InputStream stream = getClass().getClassLoader().getResourceAsStream("FunctionalGroups.xml");
			doc = new XMLDocument().readDocFromStream(stream);
			new XMLDocument().write(doc, functionalGroupsXML);
			log.info("Copied FunctionalGroups.xml to: " + functionalGroupsXML.getAbsolutePath());
		}
		log.info(doc.getRootElement().getName());
		List<Element> categoryElems = doc.selectNodes("/Categories/Category");
		log.info("Number of categories: " + categoryElems.size());
		for (Element category : categoryElems) {
			CategorizedFunctionalGroups catFGroups = new CategorizedFunctionalGroups();
			List<FunctionalGroup> functionalGroups = new ArrayList<FunctionalGroup>();
			List<Element> fgElems = category.selectNodes("FunctionalGroup");
			log.info("Number of functional groups in category " + category.attributeValue("name") + ": " + fgElems.size());
			for (Element fgElem : fgElems) {
				FunctionalGroup fGroup = new FunctionalGroup();
				fGroup.setName(fgElem.selectSingleNode("Name").getText());
				fGroup.setSMARTS(fgElem.selectSingleNode("SMARTS").getText());
				functionalGroups.add(fGroup);
			}
			catFGroups.setCategory(category.attributeValue("name"));
			catFGroups.setFunctionalGroups(functionalGroups);
			categorizedGroups.add(catFGroups);
		}

		return categorizedGroups;
	}

}
