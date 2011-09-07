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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.xml.DOMConfigurator;
import org.openscience.cdk.exception.CDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import edu.scripps.fl.pubchem.promiscuity.model.CompoundPromiscuityInfo;
import edu.scripps.fl.pubchem.promiscuity.model.FunctionalGroup;
import edu.scripps.fl.pubchem.promiscuity.model.PCPromiscuityParameters;
import edu.scripps.fl.pubchem.promiscuity.model.PromiscuityCount;
import edu.scripps.fl.pubchem.promiscuity.model.Protein;
import edu.scripps.fl.pubchem.web.entrez.EUtilsFactory;

public class TestPCPromiscuity {

	private static final Logger log = LoggerFactory.getLogger(TestPCPromiscuity.class);

	public TestPCPromiscuity() {
		DOMConfigurator.configure(TestPCPromiscuity.class.getClassLoader().getResource("log4j.config.xml"));
	}

	public static void main(String[] args) {
		TestPCPromiscuity test = new TestPCPromiscuity();
		log.info("Memory usage at program beginning: " + test.memUsage());
		PCPromiscuityParameters params = new PCPromiscuityParameters();

		params.setDatabase("pccompound");
		params.setSimpleMode(true);
		params.setPerProteinMode(false);

		try {
			// test.testCompoundSummarySetup(list, "pccompound");
			// params.setIds(Arrays.asList(new Long[]{2519L
			// }));
			params.setIds(test.getCompoundListFromTxtFiles("10_MLSMR.txt"));
			Map<Long, CompoundPromiscuityInfo> map = new PCPromiscuityMain(params).getCompoundPromiscuityInfoMap();
			String fileName = System.getProperty("user.home") + "\\PromiscuityCSV.csv";
			new PCPromiscuityOutput().compoundPromiscuityToCSV(map, params, new File(fileName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<Long> getCompoundListFromTxtFiles(String file) throws NumberFormatException, IOException {
		URL url = getClass().getClassLoader().getResource(file);
		FileInputStream in = new FileInputStream(url.getFile());
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String strLine;
		List<Long> list = new ArrayList<Long>();

		while ((strLine = br.readLine()) != null) {
			list.add(Long.parseLong(strLine.trim()));
		}
		in.close();
		return list;
	}

	public Map<Long, CompoundPromiscuityInfo> testCompoundSummarySetup(List<Long> list, String database) throws Exception {
		EUtilsFactory factory = EUtilsFactory.getInstance();
		Map<Long, CompoundPromiscuityInfo> compounds = new HashMap<Long, CompoundPromiscuityInfo>();
		PCPromiscuityFactory promFactory = new PCPromiscuityFactory();
		compounds = promFactory.getCompoundsWithDescriptors(list, database);
		for (Long keyId : compounds.keySet()) {
			CompoundPromiscuityInfo compound = compounds.get(keyId);
			printCompoundInfo(compound);
		}
		return compounds;
	}

	public void printCompoundInfo(CompoundPromiscuityInfo compound) {
		System.out.print(compound.getId());
		System.out.print(" On Hold: " + compound.getOnHold());
		Map<String, Object> props = compound.getDescriptors();
		if (props != null) {
			for (String key : props.keySet()) {
				System.out.print(" " + key + " : " + props.get(key));
			}
		}
		System.out.println();
	}

	public void testFunctionalGroupDetection(Map<Long, CompoundPromiscuityInfo> map) throws CDKException, IOException,
			ParserConfigurationException, SAXException, URISyntaxException {
		new FunctionalGroupDetectionFactory().calculateFunctionalGroups(map);
		for (Long id : map.keySet()) {
			CompoundPromiscuityInfo compound = map.get(id);
			printFunctionalGroups(compound);
		}
	}

	public void printFunctionalGroups(CompoundPromiscuityInfo compound) {
		List<FunctionalGroup> groups = compound.getFunctionalGroups();
		System.out.print("id: " + compound.getId() + " groups: ");
		if (groups != null) {
			for (FunctionalGroup group : groups) {
				System.out.print(group.getName() + " ");
			}
		}
		System.out.println();
	}

	public void testEntrezRequests(PCPromiscuityParameters params) throws Exception {
		PCPromiscuityMain main = new PCPromiscuityMain(params);

		Map<Long, CompoundPromiscuityInfo> compounds = main.getCompoundPromiscuityInfoMap();

		for (Long key : compounds.keySet()) {
			System.out.println(key);
			CompoundPromiscuityInfo compound = compounds.get(key);
			printCompoundInfo(compound);
			printFunctionalGroups(compound);
			if (compound.getOnHold()) {
				System.out.println(String.format("%s\t%s", key, "on hold"));
			} else if (!compound.getOnHold()) {
				if (!params.getPerProteinMode()) {
					Map<String, PromiscuityCount<?>> counts = compound.getCounts();
					if (counts != null) {
						printCounts(counts);
					}
				} else {
					Map<Protein, Map<String, PromiscuityCount<?>>> perProteinCountMap = compound.getPerProteinCounts();
					if (perProteinCountMap != null) {
						for (Protein protein : perProteinCountMap.keySet()) {
							System.out.println("\t" + protein.getName());
							Map<String, PromiscuityCount<?>> perProteinCounts = perProteinCountMap.get(protein);
							printCounts(perProteinCounts);
						}
					}
					Map<String, PromiscuityCount<?>> noProteinCounts = compound.getNoProteinCounts();
					if (noProteinCounts != null) {
						System.out.println("\tNo Protein Counts");
						printCounts(noProteinCounts);
					}
				}
			}
		}
	}

	private void printCounts(Map<String, PromiscuityCount<?>> counts) {
		String[] names = new String[] { "All Assays", "All Projects", "MLP Assays", "MLP Projects", "ChEMBL Assays",
				"Assays With No Protein Targets", "All Proteins", "Luciferase Assays", "Beta Lactamase Assays" };

		for (String countName : names) {
			PromiscuityCount<?> count = counts.get(countName);
			if (count != null) {
				System.out.println(String.format("\t\t%s\t%s%s%s", countName, count.getActive().size(), "/", count.getTotal().size()));
			}
		}
	}

	private long memUsage() {
		long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		return mem / 1024 / 1024;
	}
}
