#!/usr/bin/env python3
import re

def normalize(s):
	s = re.sub(r'\n', ' ', s)
	s = re.sub(r'/\*.*?\*/', ' ', s)
	s = re.sub(r'\s+', ' ', s)
	return s.strip()

def convertBiocDoc():
	normalizedCode = ""
	with open("src/GNormPluslib/BioCDoc.java") as f:
		code = f.read()
		normalizedCode = re.sub(r'//.*', '', code)
		normalizedCode = normalize(normalizedCode)
		replaceImports = 'import com.pengyifan.bioc.*;import com.pengyifan.bioc.io.BioCCollectionReader;import com.pengyifan.bioc.io.BioCCollectionWriter;import com.pengyifan.bioc.io.BioCDocumentWriter;'
		normalizedCode = re.sub('import bioc.*import bioc.*?;', '', normalizedCode)
		replace1 = normalize('ConnectorWoodstox connector = new ConnectorWoodstox(); BioCCollection collection = new BioCCollection(); try { collection = connector.startRead(new InputStreamReader(new FileInputStream(InputFile), "UTF-8")); }')
		normalizedCode = normalizedCode.replace(replace1, 'try (BioCCollectionReader bioCCollectionReader = new BioCCollectionReader(new InputStreamReader(new FileInputStream(InputFile), "UTF-8"))) {	BioCCollection collection = bioCCollectionReader.readCollection();	}')
		replace2 = normalize('String parser = BioCFactory.WOODSTOX; BioCFactory factory = BioCFactory.newFactory(parser); BioCDocumentWriter BioCOutputFormat = factory.createBioCDocumentWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));')
		normalizedCode = normalizedCode.replace(replace2, 'final BioCCollectionWriter bioCCollectionWriter = new BioCCollectionWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));')
		normalizedCode = normalizedCode.replace('BioCOutputFormat.writeCollectionInfo(biocCollection);', '')
		replace3 = normalize('BioCLocation location = new BioCLocation\(\); location.setOffset\(([^;]+)\); location.setLength\(([^;]+)\); biocAnnotation.setLocation\((location)\);')
		normalizedCode = re.sub(replace3, r'BioCLocation location = new BioCLocation(\1,\2); biocAnnotation.setLocations(Set.of(\3));', normalizedCode)
		normalizedCode = normalizedCode.replace('BioCOutputFormat.writeDocument(biocDocument);', '')
		normalizedCode = normalizedCode.replace('BioCOutputFormat.close();', 'bioCCollectionWriter.writeCollection(biocCollection); bioCCollectionWriter.close();')
		replace4 = normalize('ConnectorWoodstox connector = new ConnectorWoodstox(); BioCCollection collection = new BioCCollection(); collection = connector.startRead(new InputStreamReader(new FileInputStream(input), "UTF-8")); while (connector.hasNext()) { BioCDocument document = connector.next();')
		normalizedCode = normalizedCode.replace(replace4, 'BioCCollection collection = new BioCCollection(); final BioCCollectionReader bioCCollectionReader = new BioCCollectionReader(new InputStreamReader(new FileInputStream(input), "UTF-8")); final Iterator<BioCDocument> docIt = bioCCollectionReader.readCollection().documentIterator(); while(docIt.hasNext()) { BioCDocument document = docIt.next();')
		normalizedCode = re.sub('(getInfon\([^)]+\))',r'\1.get()',normalizedCode)
		normalizedCode = normalizedCode.replace('getText()', 'getText().get()')
		normalizedCode = normalizedCode.replace('getLocations().get(0)', 'getTotalLocation()')
		replace5 = normalize('BioCDocumentWriter BioCOutputFormat = BioCFactory.newFactory(BioCFactory.WOODSTOX).createBioCDocumentWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8")); BioCCollection biocCollection_input = new BioCCollection(); BioCCollection biocCollection_output = new BioCCollection(); ConnectorWoodstox connector = new ConnectorWoodstox(); biocCollection_input = connector.startRead(new InputStreamReader(new FileInputStream(input), "UTF-8")); BioCOutputFormat.writeCollectionInfo(biocCollection_input); int i=0; while (connector.hasNext()) { BioCDocument document_output = new BioCDocument(); BioCDocument document_input = connector.next();')
		normalizedCode = normalizedCode.replace(replace5, 'final BioCDocumentWriter BioCOutputFormat = new BioCDocumentWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8")); BioCCollection biocCollection_input = new BioCCollection(); BioCCollection biocCollection_output = new BioCCollection(); biocCollection_input = new BioCCollectionReader(new InputStreamReader(new FileInputStream(input), "UTF-8")).readCollection(); BioCOutputFormat.writeBeginCollectionInfo(biocCollection_input); int i=0; final Iterator<BioCDocument> docIt = biocCollection_input.documentIterator(); while(docIt.hasNext()) { BioCDocument document_output = new BioCDocument(); final BioCDocument document_input = docIt.next();')
		replace6 = normalize('bioCCollectionWriter.writeCollection(biocCollection); bioCCollectionWriter.close();')
		normalizedCode = normalizedCode.replace(replace6, 'BioCOutputFormat.close();')
		replace7 = normalize('BioCOutputFormat.close(); inputfile.close();')
		normalizedCode = normalizedCode.replace(replace7,'  bioCCollectionWriter.writeCollection(biocCollection); bioCCollectionWriter.close(); inputfile.close();')
	with open("src/GNormPluslib/BioCDoc2.java") as f:
		f.write(normalizedCode)

if __name__ == "__main__":
	convertBiocDoc()
	