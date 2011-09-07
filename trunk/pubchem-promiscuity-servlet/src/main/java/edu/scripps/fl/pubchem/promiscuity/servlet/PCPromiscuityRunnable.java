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
package edu.scripps.fl.pubchem.promiscuity.servlet;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import edu.scripps.fl.pubchem.promiscuity.PCPromiscuityMain;
import edu.scripps.fl.pubchem.promiscuity.PCPromiscuityOutput;
import edu.scripps.fl.pubchem.promiscuity.model.CompoundPromiscuityInfo;
import edu.scripps.fl.pubchem.promiscuity.model.PCPromiscuityParameters;

public class PCPromiscuityRunnable implements Runnable {

	private final PCPromiscuityParameters params;
	private final File tmpFile;

	public PCPromiscuityRunnable(PCPromiscuityParameters params, File tmpFile) {
		this.params = params;
		this.tmpFile = tmpFile;
	}

	public void run() {
		try {
			Map<Long, CompoundPromiscuityInfo> map = new PCPromiscuityMain(params).getCompoundPromiscuityInfoMap();
			PCPromiscuityOutput output = new PCPromiscuityOutput();
			output.compoundPromiscuityToXML(map, params, tmpFile);
			String csvName = FilenameUtils.removeExtension(tmpFile.getAbsolutePath()) +".csv";
			File file = new File(csvName);
			output.compoundPromiscuityToCSV(map, params, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
