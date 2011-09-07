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
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import edu.scripps.fl.cdk.CDKFiles;
import edu.scripps.fl.pubchem.promiscuity.model.CompoundPromiscuityInfo;
import edu.scripps.fl.pubchem.promiscuity.model.FunctionalGroup;

public class FunctionalGroupDetectionFactory {

	private static final Logger log = LoggerFactory.getLogger(FunctionalGroupDetectionFactory.class);

	public FunctionalGroupDetectionFactory() {

	}

	private boolean isSubStructure(String smiles1, String smiles2) throws CDKException, IOException {
		IMolecule mol1 = CDKFiles.getMolecule(smiles1);
		IMolecule mol2 = CDKFiles.getMolecule(smiles2);
		return UniversalIsomorphismTester.isSubgraph(mol1, mol2);
	}

	public void calculateFunctionalGroups(Map<Long, CompoundPromiscuityInfo> compounds) throws CDKException, IOException,
			ParserConfigurationException, SAXException, URISyntaxException {
		List<FunctionalGroup> groups = GetFunctionalGroups();

		for (Long id : compounds.keySet()) {
			CompoundPromiscuityInfo compound = compounds.get(id);
			if (!compound.getOnHold()) {
				List<FunctionalGroup> compoundGroups = new ArrayList<FunctionalGroup>();
				String smiles = (String) compound.getDescriptors().get("CanonicalSmiles");
				if (smiles != null) {
					for (FunctionalGroup group : groups) {
						if (isSubStructure(smiles, group.getSmiles()))
							compoundGroups.add(group);
					}
					compound.setFunctionalGroups(compoundGroups);
				}
			}
			compounds.put(id, compound);
		}
	}

	private List<FunctionalGroup> GetFunctionalGroups() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
		List<FunctionalGroup> functionalGroups = new ArrayList<FunctionalGroup>();

		String fileName = System.getProperty("user.home") + "\\FunctionalGroups.xml";
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
		List<Element> functionalGroupsElems = doc.selectNodes("/functionalGroups/functionalGroup");
		for (Element element : functionalGroupsElems) {
			FunctionalGroup fGroup = new FunctionalGroup();
			fGroup.setName(element.selectSingleNode("name").getText());
			fGroup.setSmiles(element.selectSingleNode("smiles").getText());
			functionalGroups.add(fGroup);
		}

		return functionalGroups;
	}

}
