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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.promiscuity.model.CategorizedFunctionalGroups;
import edu.scripps.fl.pubchem.promiscuity.model.CompoundPromiscuityInfo;
import edu.scripps.fl.pubchem.promiscuity.model.FunctionalGroup;
import edu.scripps.fl.pubchem.promiscuity.model.PCPromiscuityParameters;
import edu.scripps.fl.pubchem.promiscuity.model.PromiscuityCount;
import edu.scripps.fl.pubchem.promiscuity.model.Protein;
import edu.scripps.fl.rhinoproject.JSProcessor;

public class PCPromiscuityOutput {

	private static final Logger log = LoggerFactory.getLogger(PCPromiscuityOutput.class);
	
	private JSProcessor jsp = null;

	public static final String[] names = new String[] { PCPromiscuityFactory.allAssayName, PCPromiscuityFactory.allProjectsName,
			PCPromiscuityFactory.mlpAssaysName, PCPromiscuityFactory.mlpProjectsNames, PCPromiscuityFactory.chemblName,
			PCPromiscuityFactory.noProteinsName, PCPromiscuityFactory.allProteinsName, PCPromiscuityFactory.luciferaseName,
			PCPromiscuityFactory.betaLactamaseName, PCPromiscuityFactory.fluorescentName };

	public static final String[] descriptorNames = { "MinAC", "MaxAC", "MinTC", "MaxTC", PCPromiscuityFactory.cSmiles,
			PCPromiscuityFactory.mWeight, PCPromiscuityFactory.xLogP, PCPromiscuityFactory.hBondDonor, PCPromiscuityFactory.hBondAcceptor,
			"TPSA", "Complexity", "RotatableBondCount", "MolecularFormula", "TotalFormalCharge", "HeavyAtomCount", "AtomChiralCount",
			"AtomChiralDefCount", "AtomChiralUndefCount", "BondChiralCount", "BondChiralDefCount", "BondChiralUndefCount",
			"IsotopeAtomCount", "CovalentUnitCount", "TautomerCount", PCPromiscuityFactory.ruleFiveName };
	public static final String[] functionalGroupCategories = {"PAINS_A", "PAINS_B", "PAINS_C"};

	public void compoundPromiscuityToCSV(Map<Long, CompoundPromiscuityInfo> map, PCPromiscuityParameters params, File file)
			throws Exception {
		String[] countColumns = names;
		if (params.getSimpleMode())
			countColumns = new String[] { names[0] };
		String[] descriptorColumns = descriptorNames;
		List<Long> ids = params.getIds();
		Writer out = new OutputStreamWriter(new FileOutputStream(file));

		if (params.getDatabase().equals("pcsubstance"))
			out.write("SID,");
		out.write("CID");
		for(String fgCat: functionalGroupCategories){
			out.write("," + fgCat);
		}
		out.write(",PossibleFalseAromaticityDetection");
		for (String cc : descriptorColumns) {
			out.write("," + cc);
		}
		if (params.getPerProteinMode())
			out.write(",Protein");
		for (String cc : countColumns) {
			out.write("," + cc + ",");
		}
		out.write("\n,");
		printExtraCommas(params, descriptorColumns, out);
		for (String cc : countColumns) {
			out.write(",Active,Total");
		}
		out.write("\n");
		for (Long id : ids) {
			out.write(id.toString());

			CompoundPromiscuityInfo cpInfo = map.get(id);
			if (cpInfo == null) {
				out.write(", unable to get compound information.");
				out.write("\n");
				continue;
			}
			if (cpInfo.getOnHold()) {
				out.write(", On Hold");
				out.write("\n");
				continue;
			}

			printDescriptors(params, cpInfo, out, descriptorColumns);

			if (!params.getPerProteinMode()) {
				Map<String, PromiscuityCount<?>> counts = cpInfo.getCounts();
				printCSVCount(counts, countColumns, out);
			} else {
				Map<Protein, Map<String, PromiscuityCount<?>>> proteinCounts = cpInfo.getPerProteinCounts();
				int count = 0;
				for (Entry<Protein, Map<String, PromiscuityCount<?>>> proteinCount : proteinCounts.entrySet()) {
					if (count > 0)
						printExtraCommas(params, descriptorColumns, out);
					out.write(",\"" + proteinCount.getKey().getName() + "\"");
					printCSVCount(proteinCount.getValue(), countColumns, out);
					count = count + 1;
				}
				Map<String, PromiscuityCount<?>> noProteinCounts = cpInfo.getNoProteinCounts();
				if (count > 0)
					printExtraCommas(params, descriptorColumns, out);
				out.write(", No Proteins");
				printCSVCount(noProteinCounts, countColumns, out);
			}
		}
		out.close();
	}

	private void printExtraCommas(PCPromiscuityParameters params, String[] descriptorColumns, Writer out) throws IOException {
		if (params.getDatabase().equals("pcsubstance"))
			out.write(",");
		for(String fg: functionalGroupCategories){
			out.write(",");
		}
		for (String cc : descriptorColumns) {
			out.write(",");
		}
		if (params.getPerProteinMode())
			out.write(",");
	}

	private void printDescriptors(PCPromiscuityParameters params, CompoundPromiscuityInfo cpInfo, Writer out, String[] descriptorColumns)
			throws IOException {
		if (params.getDatabase().equals("pcsubstance")){
			if(cpInfo.getCID() != null)
				out.write("," + cpInfo.getCID());
			else
				out.write(",");
		}
		
		Map<String, Object> descriptors = cpInfo.getDescriptors();
		Map<String, CategorizedFunctionalGroups> categorizedFGMap = cpInfo.getCategorizedFunctionalGroupsMap();
		for(String category: functionalGroupCategories){
			CategorizedFunctionalGroups cFGs = categorizedFGMap.get(category);
			if(cFGs != null){
				out.write(",\"");
				List<FunctionalGroup> fgs = cFGs.getFunctionalGroups();
				List<String> groups = new ArrayList<String>();
				for (FunctionalGroup group : fgs) {
					groups.add(group.getName());
				}
				out.write(StringUtils.join(groups, ","));
				out.write("\"");
			}
			else{
				out.write(",");
			}
		}
		
		if(cpInfo.isPossibleFalseAromaticityDetection())
        	out.write(",true");
		else
			out.write(",");
        
		for (String cc : descriptorColumns) {
			if(descriptors.get(cc) !=null)
				out.write(", " + descriptors.get(cc));
			else
				out.write(", ");
		}
	}

	private void printCSVCount(Map<String, PromiscuityCount<?>> counts, String[] countColumns, Writer out) throws IOException {

		for (String cc : countColumns) {
			PromiscuityCount<?> count = counts.get(cc);
			if (count != null)
				out.write("," + count.getActive().size() + "," + count.getTotal().size());
			else
				out.write(",");
		}
		out.write("\n");
	}

	public void compoundPromiscuityToXML(Map<Long, CompoundPromiscuityInfo> map, PCPromiscuityParameters params, File file)
			throws Exception {
	    
	    jsp = new JSProcessor();
	    jsp.init();
        try{
                        
            InputStream is = getClass().getResourceAsStream("/compress.txt");
            jsp.setCodeSource(new InputStreamReader(is));
            
            String[] descriptorColumns = descriptorNames;
            List<Long> ids = params.getIds();

            URL url = getClass().getClassLoader().getResource("Result.xml");
            Document doc = new XMLDocument().readDocFromURL(url);

            Element root = doc.getRootElement();
            String db = params.getDatabase();
            String idString = "SID";
            if (db.equalsIgnoreCase("pccompound"))
                idString = "CID";
            for (Long id : ids) {
                Element result = root.addElement("Result");
                result.addElement(idString).addText(id.toString());

                CompoundPromiscuityInfo cpInfo = map.get(id);
                if (cpInfo == null)
                    result.addElement("NoResults").addText("Error Processing this compound.");
                else {
                    if (cpInfo.getOnHold())
                        result.addElement("OnHold").addText("True");
                    else {
                        if (db.equalsIgnoreCase("pcsubstance")){
                            Element CIDe = result.addElement("CID");
                            if(cpInfo.getCID() != null)
                                CIDe.addText(cpInfo.getCID().toString());
                        }

                        Map<String, Object> descriptors = cpInfo.getDescriptors();
                        Map<String, CategorizedFunctionalGroups> categorizedFGMap = cpInfo.getCategorizedFunctionalGroupsMap();

                        Element descriptorsE = result.addElement("Descriptors");
                        for(String category: functionalGroupCategories){
                        	Element fgCategoryE = descriptorsE.addElement(category);
                			CategorizedFunctionalGroups cFGs = categorizedFGMap.get(category);
                			if(cFGs != null){
                				List<FunctionalGroup> fgs = cFGs.getFunctionalGroups();
                				List<String> groups = new ArrayList<String>();
                				for (FunctionalGroup group : fgs) {
                					groups.add(group.getName());
                				}
                				fgCategoryE.addText(StringUtils.join(groups, ", "));
                			}
                        }
                        Element possibleFalse = descriptorsE.addElement("PossibleFalseAromaticityDetection");
                        if(cpInfo.isPossibleFalseAromaticityDetection())
                        	possibleFalse.addText("true");
                        
                        for (String cc : descriptorColumns) {
                            Element descriptorCC = descriptorsE.addElement(StringUtils.remove(cc, " "));
                            if(descriptors.get(cc) != null)
                            	descriptorCC.addText(descriptors.get(cc).toString());
                        }

                        Element proteinsE = result.addElement("Proteins");
                        if (params.getPerProteinMode()) {
                            Map<Protein, Map<String, PromiscuityCount<?>>> proteinCounts = cpInfo.getPerProteinCounts();
                            Set<Protein> proteins = proteinCounts.keySet();
                            for (Protein protein : proteins) {
                                Element proteinE = proteinsE.addElement("Protein");
                                proteinE.addElement("Name").addText(protein.getName());
                                Element promiscuityCountsE = proteinE.addElement("PromiscuityCounts");
                                addCounts(promiscuityCountsE, proteinCounts.get(protein), id, db);
                            }
                            Map<String, PromiscuityCount<?>> noProteinCounts = cpInfo.getNoProteinCounts();
                            Element noProtein = proteinsE.addElement("Protein");
                            noProtein.addElement("Name").addText("");
                            Element noProteinCountsE = noProtein.addElement("PromiscuityCounts");
                            addCounts(noProteinCountsE, noProteinCounts, id, db);
                            root.addAttribute("format", "protein");
                        } else {
                            Map<String, PromiscuityCount<?>> counts = cpInfo.getCounts();
                            Element allProteins = proteinsE.addElement("Protein");
                            allProteins.addElement("Name").addText("All Proteins");
                            Element allProteinCountsE = allProteins.addElement("PromiscuityCounts");
                            addCounts(allProteinCountsE, counts, id, db);
                            root.addAttribute("format", "compound");
                        }
                    }
                }

            }
            new XMLDocument().write(doc, file);
            log.info("Finished writing xml to: " + file.getAbsolutePath());
        }
        finally{
            jsp.exit();
        }   
	}

	private void addCounts(Element element, Map<String, PromiscuityCount<?>> counts, Long id, String db) throws Exception {
	    	    
		String[] countColumns = names;
		for (String cc : countColumns) {
			PromiscuityCount<?> count = counts.get(cc);
			if (count != null) {
				Element countElem = element.addElement(cc);
				Element totalE = countElem.addElement("Total");
				String value = "0";

				List<?> totalCount = count.getTotal();
				if (totalCount != null)
					value = String.valueOf(totalCount.size());
				totalE.addElement("Count").addText(value);
				addCountElementURL(count, totalCount, totalE, id, db);

				Element activeE = countElem.addElement("Active");
				List<?> activeCount = count.getActive();

				if (activeCount != null)
					value = String.valueOf(activeCount.size());
				else
					value = "0";

				activeE.addElement("Count").addText(value);
				addCountElementURL(count, activeCount, activeE, id, db);
			}
		}
	}

	private void addCountElementURL(PromiscuityCount<?> count, List<?> list, Element elem, Long id, String db) throws Exception {
		if (list.size() > 0) {
			String urlS;
			if (count.getName().equals(PCPromiscuityFactory.allProteinsName)) {
				List<Long> proteins = new ArrayList<Long>();
				for (Object protein : list) {
					proteins.add(((Protein) protein).getGi());
				}
				// urlS = "http://www.ncbi.nlm.nih.gov/protein/" +
				// StringUtils.join(proteins, ",");
				urlS = "http://pubchem.ncbi.nlm.nih.gov/assay/assaytool.cgi?q=tgt&gi=%s&%sid=%s";
				if (db.equalsIgnoreCase("pcsubstance"))
					urlS = String.format(urlS, StringUtils.join(proteins, ","), "s", id);
				else
					urlS = String.format(urlS, StringUtils.join(proteins, ","), "c", id);
			} else if (count.getName().contains("Project")) {
				urlS = "http://www.ncbi.nlm.nih.gov/sites/entrez?db=pcassay&term=" + StringUtils.join(list, ",");
			} else {
				urlS = "http://pubchem.ncbi.nlm.nih.gov/assay/assay.cgi?p=datatable&q=sidsr&qfile=&service=&similarity=&cmpdv=%s&AIDlist=%s&%sIDlist=%s";
				if (db.equalsIgnoreCase("pcsubstance"))
					urlS = String.format(urlS, "sub", StringUtils.join(list, ","), "S", id);
				else
					urlS = String.format(urlS, "cmpd", StringUtils.join(list, ","), "C", id);
			}
			// Compress URL using deflator and base64 encoding. URL will be decompress on the client side.
			urlS = jsp.deflate(urlS);
			elem.addElement("URL").addText(urlS);
		}
	}

}
