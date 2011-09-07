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
package edu.scripps.fl.cdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.tools.HydrogenAdder;

public class CDKFiles {

	public static IMolecule getMoleculeFromFactory(Reader reader) throws CDKException, IOException {
		IMolecule mol = null;
		DefaultChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		ReaderFactory readerFactory = new ReaderFactory();
		System.out.println(readerFactory.guessFormat((BufferedReader) reader).getFormatName());
		IChemObjectReader chemReader = readerFactory.createReader(reader);
		IChemFile content = (IChemFile) chemReader.read(builder.newChemFile());
		if (content == null)
			return null;
		Iterator iter = content.chemSequences();
		if (iter.hasNext())
			mol = (IMolecule) iter.next();
		return mol;
	}

	public static IMolecule getMolecule(String structure) throws CDKException, IOException {
		return getMolecule(structure, true);
	}

	public static IMolecule getMolecule(String structure, boolean addImplicitHydrogens) throws CDKException, IOException {
		IMolecule mol = null;
		DefaultChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		try {
			SMILESReader sr = new SMILESReader(new StringReader(structure));
			MoleculeSet set = (MoleculeSet) sr.read(builder.newMoleculeSet());
			mol = set.getMolecule(0);
			if (mol.getAtomCount() == 0)
				mol = getMoleculeFromFactory(new StringReader(structure));

		} catch (CDKException notSmiles) {
			mol = getMoleculeFromFactory(new StringReader(structure));
		}
		if (addImplicitHydrogens) {
			HydrogenAdder adder = new HydrogenAdder();
			adder.addImplicitHydrogensToSatisfyValency(mol);
		}
		return mol;
	}
}
