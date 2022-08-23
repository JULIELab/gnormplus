package GNormPluslib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import GNormPluslib.PrefixTree;
import GNormPluslib.GNR;
import GNormPluslib.SR;

public class GNormPlus
{
	public static BioCDoc BioCDocobj = new BioCDoc();
	public static PrefixTree PT_Species = new PrefixTree();
	public static PrefixTree PT_Cell = new PrefixTree();
	public static PrefixTree PT_CTDGene = new PrefixTree();
	public static PrefixTree PT_Gene = new PrefixTree();
	public static PrefixTree PT_GeneChromosome = new PrefixTree();
	public static PrefixTree PT_FamilyName = new PrefixTree();
	public static HashMap<String, String> ent_hash = new HashMap<String, String>();
	public static HashMap<String, String> GenusID_hash = new HashMap<String, String>();
	public static HashMap<String, String> PrefixID_hash = new HashMap<String, String>();
	public static HashMap<String, Double> TaxFreq_hash = new HashMap<String, Double>();
	public static HashMap<String, String> GeneScoring_hash = new HashMap<String, String>();
	public static HashMap<String, Double> GeneScoringDF_hash = new HashMap<String, Double>();
	public static HashMap<String, String> GeneIDs_hash = new HashMap<String, String>();
	public static HashMap<String, String> Normalization2Protein_hash = new HashMap<String, String>();
	public static HashMap<String, String> HomologeneID_hash = new HashMap<String, String>();
	public static HashMap<String,String> SuffixTranslationMap_hash = new HashMap<String,String>();
	public static HashMap<String,String> SuffixTranslationMap2_hash = new HashMap<String,String>();
	public static HashMap<String, String> Pmid2Abb_hash = new HashMap<String, String>();
	public static HashMap<String, String> PmidAbb2LF_lc_hash = new HashMap<String, String>();
	public static HashMap<String, String> PmidLF2Abb_lc_hash = new HashMap<String, String>();
	public static HashMap<String, String> PmidAbb2LF_hash = new HashMap<String, String>();
	public static HashMap<String, String> PmidLF2Abb_hash = new HashMap<String, String>();
	public static HashMap<String, String> Pmid2ChromosomeGene_hash = new HashMap<String, String>();
	public static HashMap<String, String> SimConceptMention2Type_hash = new HashMap<String, String>();
	public static HashMap<String, String> Filtering_hash = new HashMap<String, String>();
	public static HashMap<String, String> Filtering_WithLongForm_hash = new HashMap<String, String>();
	public static HashMap<String, String> SP_Virus2Human_hash = new HashMap<String, String>();
	public static HashMap<String, String> GeneWithoutSPPrefix_hash = new HashMap<String, String>();
	public static ArrayList <String> taxid4gene = new ArrayList <String>();
	public static HashMap<String, String> setup_hash = new HashMap<String, String>();
	public static HashMap<String, String> suffixprefix_orig2modified = new HashMap<String, String>();
	public static HashMap<String, String> Abb2Longformtok_hash = new HashMap<String, String>();
	public static HashMap<String, String> StrainID_ancestor2tax_hash = new HashMap<String, String>();
	public static HashMap<String, String> StrainID_taxid2names_hash = new HashMap<String, String>();
	
	public static String SetupFile = "setup.txt";
	public static void main(String [] args) throws IOException, InterruptedException, XMLStreamException, SQLException 
	{
		String InputFolder="input";
		String OutputFolder="output";
		String FocusSpecies = "";
		if(args.length<2)
		{
			System.out.println("\n$ java -Xmx30G -Xms10G -jar GNormPlus.jar [InputFolder] [OutputFolder] [SetupFile]");
			System.out.println("[InputFolder] Default : input");
			System.out.println("[OutputFolder] Default : output");
			System.out.println("[SetupFile] Default : setup.txt\n\n");
		}
		else
		{
			/*
			 * Parameters
			 */
			InputFolder=args[0];
			OutputFolder=args[1];
			if(args.length>=3)
			{
				SetupFile = args[2];
			}
			if(args.length>=4)
			{
				FocusSpecies=args[3];
			}
		}
		
		BufferedReader br = new BufferedReader(new FileReader(SetupFile));
		String line="";
		Pattern ptmp = Pattern.compile("^	([A-Za-z0-9]+) = ([^ \\t\\n\\r]+)$");
		while ((line = br.readLine()) != null)  
		{
			Matcher mtmp = ptmp.matcher(line);
			if(mtmp.find())
			{
				setup_hash.put(mtmp.group(1), mtmp.group(2));
			}
		}
		br.close();
		if(!setup_hash.containsKey("GeneIDMatch"))
		{
			setup_hash.put("GeneIDMatch","True");
		}
		if(!setup_hash.containsKey("HomologeneID"))
		{
			setup_hash.put("HomologeneID","False");
		}
		if(!FocusSpecies.equals(""))
		{
			setup_hash.put("FocusSpecies",FocusSpecies);
		}
		if((setup_hash.get("SpeciesAssignmentOnly").equals("True")) || (setup_hash.get("GeneNormalizationOnly").equals("True")))
		{
			setup_hash.put("IgnoreNER","True");
		}
		if(!setup_hash.containsKey("ShowUnNormalizedMention"))
		{
			setup_hash.put("ShowUnNormalizedMention","False");
		}
		/*
		 * Time stamp - start : All
		 */
		double startTime,endTime,totTime;
		startTime = System.currentTimeMillis();//start time
	
		int NumFiles=0;
		File folder = new File(InputFolder);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++)
		{
			if (listOfFiles[i].isFile()) 
			{
				String InputFile = listOfFiles[i].getName();
				File f = new File(OutputFolder+"/"+InputFile);
				if(f.exists() && !f.isDirectory()) 
				{ 
				}
				else
				{
					NumFiles++;
				}
			}
		}
		
		System.out.println("Total "+NumFiles+" file(s) wait(s) for process.");

		if(NumFiles>0)
		{
			/* 
			 * Start & Load Dictionary
			 */
			String TrainTest = "Test";
			if(setup_hash.containsKey("TrainTest"))
			{
				TrainTest = setup_hash.get("TrainTest");
			}
			
			System.out.print("Loading Gene/Species Dictionary : Processing ... \r");
			/** Load Dictionary */
			{
				/** GeneWithoutSPPrefix */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/GeneWithoutSPPrefix.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					GeneWithoutSPPrefix_hash.put(line, "");
				}
				br.close();	
				
				/** CTDGene  */
				if(setup_hash.containsKey("IgnoreNER") && setup_hash.get("IgnoreNER").toLowerCase().equals("true")){} // not NER (entities are pre-annotated)
				else if(setup_hash.containsKey("SpeciesAssignmentOnly") && setup_hash.get("SpeciesAssignmentOnly").toLowerCase().equals("true")) {} // species assignment 
				else
				{
					PT_CTDGene.TreeFile2Tree(setup_hash.get("DictionaryFolder")+"/PT_CTDGene.txt");
				}
				/** ent */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/ent.rev.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					String l[]=line.split("\t"); //&#x00391;	Alpha
					ent_hash.put(l[0], l[1]);
				}
				br.close();	
	
				/** FamilyName */
				if((!setup_hash.containsKey("IgnoreNER")) || setup_hash.get("IgnoreNER").toLowerCase() != "true")
				{
					PT_FamilyName.TreeFile2Tree(setup_hash.get("DictionaryFolder")+"/PT_FamilyName.txt");
				}
				
				/** Species */
				PT_Species.TreeFile2Tree(setup_hash.get("DictionaryFolder")+"/PT_Species.txt");

				/** Cell */
				PT_Cell.TreeFile2Tree(setup_hash.get("DictionaryFolder")+"/PT_Cell.txt");
				
				/** Genus */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/SPGenus.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					String l[]=line.split("\t");
					GenusID_hash.put(l[0], l[1]); // tax id -> Genus
				}
				br.close();	
				
				/** taxid4gene */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/tax4gene.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					taxid4gene.add(line); // tax id -> Genus
				}
				br.close();	
				
				/** gene_prefix & gene_suffix */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/PrefixSuffix.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					String l[]=line.split("\t");
					String org=l[0];
					String mod=l[1];
					suffixprefix_orig2modified.put(org,mod);
				}
				br.close();	
				
				/** gene_prefix & gene_suffix */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/NonGeneAbbr.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					String l[]=line.split("\t");
					String shortform=l[0];
					String longform_toks=l[1];
					Abb2Longformtok_hash.put(shortform,longform_toks);
				}
				br.close();
																								
				/** Prefix */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/SPPrefix.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					String l[]=line.split("\t");
					PrefixID_hash.put(l[0], l[1]); //tax id -> prefix
				}
				br.close();
				PrefixID_hash.put("9606", "h");
				PrefixID_hash.put("10090", "m");
				PrefixID_hash.put("10116", "r");
				PrefixID_hash.put("4932", "y");
				PrefixID_hash.put("7227", "d");
				PrefixID_hash.put("7955", "z|dr|Dr|Zf|zf");
				PrefixID_hash.put("3702", "at|At");
				
				/** Frequency */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/taxonomy_freq.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					String l[]=line.split("\t");
					TaxFreq_hash.put(l[0], Double.parseDouble(l[1])/200000000); //tax id -> prefix
				}
				br.close();	
				
				/** SP_Virus2Human_hash */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/SP_Virus2HumanList.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					SP_Virus2Human_hash.put(line,"9606");
				}
				br.close();	
	
				/** SimConcept.MentionType */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/SimConcept.MentionType.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					String l[]=line.split("\t");
					SimConceptMention2Type_hash.put(l[0], l[1]);
				}
				br.close();	
				
				/** Filtering */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/Filtering.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					Filtering_hash.put(line, "");
				}
				br.close();	
				/** Filtering_WithLongForm.txt */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/Filtering_WithLongForm.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					String l[]=line.split("\t");
					Filtering_WithLongForm_hash.put(l[0], l[1]);
				}
				br.close();	
					
				/** SPStrain */
				br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/SPStrain.txt"));
				line="";
				while ((line = br.readLine()) != null)  
				{
					String l[]=line.split("\t");
					String ancestor_id = l[0];
					String tax_id = l[1];
					String tax_names = l[2];
					StrainID_ancestor2tax_hash.put(ancestor_id, tax_id); // ancestor -> tax_id
					StrainID_taxid2names_hash.put(tax_id, tax_names); // tax id -> strain
				}
				br.close();
				
				if((!setup_hash.containsKey("IgnoreNER")) || setup_hash.get("IgnoreNER").toLowerCase() != "true")
				{
					/** Gene */
					if(setup_hash.containsKey("FocusSpecies") && !setup_hash.get("FocusSpecies").equals("All"))
					{
						PT_Gene.TreeFile2Tree(setup_hash.get("DictionaryFolder")+"/PT_Gene."+setup_hash.get("FocusSpecies")+".txt");
					}
					else if((!FocusSpecies.equals("")) && (!FocusSpecies.equals("All")))
					{
						PT_Gene.TreeFile2Tree(setup_hash.get("DictionaryFolder")+"/PT_Gene."+FocusSpecies+".txt");
					}
					else
					{
						PT_Gene.TreeFile2Tree(setup_hash.get("DictionaryFolder")+"/PT_Gene.txt");
					}
				
					/** GeneScoring */
					String FileName=setup_hash.get("DictionaryFolder")+"/GeneScoring.txt";
					
					if(setup_hash.containsKey("FocusSpecies") && !setup_hash.get("FocusSpecies").equals("All"))
					{
						FileName = setup_hash.get("DictionaryFolder")+"/GeneScoring."+setup_hash.get("FocusSpecies")+".txt";
					}
					else if((!FocusSpecies.equals("")) && (!FocusSpecies.equals("All")))
					{
						FileName = setup_hash.get("DictionaryFolder")+"/GeneScoring."+FocusSpecies+".txt";
					}
					br = new BufferedReader(new FileReader(FileName));
					line="";
					while ((line = br.readLine()) != null)  
					{
						String l[]=line.split("\t");
						GeneScoring_hash.put(l[0], l[1]+"\t"+l[2]+"\t"+l[3]+"\t"+l[4]+"\t"+l[5]+"\t"+l[6]);
					}
					br.close();	
					
					/** GeneScoring.DF */
					FileName=setup_hash.get("DictionaryFolder")+"/GeneScoring.DF.txt";
					if(setup_hash.containsKey("FocusSpecies") && !setup_hash.get("FocusSpecies").equals("All"))
					{
						FileName = setup_hash.get("DictionaryFolder")+"/GeneScoring.DF."+setup_hash.get("FocusSpecies")+".txt";
					}
					else if((!FocusSpecies.equals("")) && (!FocusSpecies.equals("All")))
					{
						FileName = setup_hash.get("DictionaryFolder")+"/GeneScoring.DF."+FocusSpecies+".txt";
					}
					br = new BufferedReader(new FileReader(FileName));
					double Sum = Double.parseDouble(br.readLine());
					while ((line = br.readLine()) != null)  
					{
						String l[]=line.split("\t");
						// token -> idf
						GeneScoringDF_hash.put(l[0], Math.log10(Sum/Double.parseDouble(l[1])));
					}
					br.close();
				}
				
				/** Suffix Translation */
				SuffixTranslationMap_hash.put("alpha","a");
				SuffixTranslationMap_hash.put("a","alpha");
				SuffixTranslationMap_hash.put("beta","b");
				SuffixTranslationMap_hash.put("b","beta");
				SuffixTranslationMap_hash.put("delta","d");
				SuffixTranslationMap_hash.put("d","delta");
				SuffixTranslationMap_hash.put("z","zeta");
				SuffixTranslationMap_hash.put("zeta","z");
				SuffixTranslationMap_hash.put("gamma","g");
				SuffixTranslationMap_hash.put("g","gamma");
				SuffixTranslationMap_hash.put("r","gamma");
				SuffixTranslationMap_hash.put("y","gamma");
				
				SuffixTranslationMap2_hash.put("2","ii");
				SuffixTranslationMap2_hash.put("ii","2");
				SuffixTranslationMap2_hash.put("II","2");
				SuffixTranslationMap2_hash.put("1","i");
				SuffixTranslationMap2_hash.put("i","1");
				SuffixTranslationMap2_hash.put("I","1");
				
				/** GeneID */
				if(setup_hash.containsKey("GeneIDMatch") && setup_hash.get("GeneIDMatch").toLowerCase().equals("true"))
				{
					br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/GeneIDs.txt"));
					line="";
					while ((line = br.readLine()) != null)  
					{
						String l[]=line.split("\t");
						GeneIDs_hash.put(l[0],l[1]);
					}
					br.close();
				}
				
				/** Normalization2Protein */
				if(setup_hash.containsKey("Normalization2Protein") && setup_hash.get("Normalization2Protein").toLowerCase().equals("true"))
				{
					br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/Gene2Protein.txt"));
					line="";
					while ((line = br.readLine()) != null)  
					{
						String l[]=line.split("\t");
						Normalization2Protein_hash.put(l[0],l[1]);
					}
					br.close();
				}
				
				/** HomologeneID */
				if(setup_hash.containsKey("HomologeneID") && setup_hash.get("HomologeneID").toLowerCase().equals("true"))
				{
					br = new BufferedReader(new FileReader(setup_hash.get("DictionaryFolder")+"/Gene2Homoid.txt"));
					line="";
					while ((line = br.readLine()) != null)  
					{
						String l[]=line.split("\t");
						HomologeneID_hash.put(l[0],l[1]);
					}
					br.close();
				}
				
				/** GeneChromosome */
				//PT_GeneChromosome.TreeFile2Tree(setup_hash.get("DictionaryFolder")+"/PT_GeneChromosome.txt");
			}
			endTime = System.currentTimeMillis();
			totTime = endTime - startTime;
			System.out.println("Loading Gene Dictionary : Processing Time:"+totTime/1000+"sec");
			
			folder = new File(InputFolder);
			listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++)
			{
				if (listOfFiles[i].isFile()) 
				{
					String InputFile = listOfFiles[i].getName();
					System.out.println("Processing " + InputFile);
					File f = new File(OutputFolder+"/"+InputFile);
					if(f.exists() && !f.isDirectory()) 
					{ 
						System.out.println(InputFolder+"/"+InputFile+" - Done. (The output file exists in output folder)");
					}
					else
					{
						BioCDocobj = new BioCDoc();
						
						/*
						 * Format Check 
						 */
						String Format = "";
						String checkR = BioCDocobj.BioCFormatCheck(InputFolder+"/"+InputFile);
						if(checkR.equals("BioC"))
						{
							Format = "BioC";
						}
						else if(checkR.equals("PubTator"))
						{
							Format = "PubTator";
						}
						else
						{
							System.out.println(checkR);
							System.exit(0);
						}
						
						System.out.print(InputFolder+"/"+InputFile+" - ("+Format+" format) : Processing ... \r");
						/*
						 * GNR
						 */
						if(setup_hash.containsKey("IgnoreNER")  && setup_hash.get("IgnoreNER").toLowerCase().equals("true") ) // pre-annotated name entities
						{
							if(Format.equals("PubTator"))
							{
								BioCDocobj.PubTator2BioC(InputFolder+"/"+InputFile,"tmp/"+InputFile);
								br = new BufferedReader(new FileReader("tmp/"+InputFile));
								BufferedWriter fr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("tmp/"+InputFile+".GNR.xml"), "UTF-8"));
								line="";
								while ((line = br.readLine()) != null)  
								{
									fr.write(line);
								}
								br.close();
								fr.close();
							}
							else if(Format.equals("BioC"))
							{
								br = new BufferedReader(new FileReader(InputFolder+"/"+InputFile));
								BufferedWriter fr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("tmp/"+InputFile+".GNR.xml"), "UTF-8"));
								line="";
								while ((line = br.readLine()) != null)  
								{
									fr.write(line);
								}
								br.close();
								fr.close();
							}
							GNR GNRobj = new GNR();
							GNormPlus.BioCDocobj.BioCReaderWithAnnotation("tmp/"+InputFile+".GNR.xml");
							GNRobj.Ab3P("tmp/"+InputFile+".GNR.xml","tmp/"+InputFile+".Abb",TrainTest);
						}
						else
						{
							GNR GNRobj = new GNR();
							
							if(TrainTest.equals("Test") || TrainTest.equals("Train"))//Test & Train
							{
								if(Format.equals("PubTator"))
								{
									BioCDocobj.PubTator2BioC(InputFolder+"/"+InputFile,"tmp/"+InputFile);
									GNRobj.LoadInputFile("tmp/"+InputFile,"tmp/"+InputFile+".Abb",TrainTest);
								}
								else if(Format.equals("BioC"))
								{
									GNRobj.LoadInputFile(InputFolder+"/"+InputFile,"tmp/"+InputFile+".Abb",TrainTest);
								}
								
								GNRobj.FeatureExtraction("tmp/"+InputFile+".data","tmp/"+InputFile+".loca",TrainTest);
								
								if(TrainTest.equals("Test"))
								{
									GNRobj.CRF_test(setup_hash.get("GNRModel"),"tmp/"+InputFile+".data","tmp/"+InputFile+".output","top3"); //top3
									
									if(Format.equals("PubTator"))
									{
										GNRobj.ReadCRFresult("tmp/"+InputFile,"tmp/"+InputFile+".loca","tmp/"+InputFile+".output","tmp/"+InputFile+".GNR.xml",0.005,0.05); //0.005,0.05
										GNRobj.PostProcessing("tmp/"+InputFile,"tmp/"+InputFile+".GNR.xml");
									}
									else if(Format.equals("BioC"))
									{
										GNRobj.ReadCRFresult(InputFolder+"/"+InputFile,"tmp/"+InputFile+".loca","tmp/"+InputFile+".output","tmp/"+InputFile+".GNR.xml",0.005,0.05); //0.005,0.05
										System.out.println("And now: Post Processing");
										GNRobj.PostProcessing(InputFolder+"/"+InputFile,"tmp/"+InputFile+".GNR.xml");
									}
								}
							}
							else if( (setup_hash.containsKey("IgnoreNER") && setup_hash.get("IgnoreNER").toLowerCase().equals("true") ) || 
									TrainTest.equals("TrainSC")
									) // IgnoreNER & Train & TrainSC
							{
								if(Format.equals("PubTator"))
								{
									BioCDocobj.PubTator2BioC(InputFolder+"/"+InputFile,"tmp/"+InputFile);
									GNRobj.LoadInputFile("tmp/"+InputFile,"tmp/"+InputFile+".Abb","Train");
									GNRobj.PostProcessing("tmp/"+InputFile,"tmp/"+InputFile+".GNR.xml");
								}
								else if(Format.equals("BioC"))
								{
									GNRobj.LoadInputFile(InputFolder+"/"+InputFile,"tmp/"+InputFile+".Abb","Train");
									System.out.println("And now: Post Processing mit IgnoreNER");
									GNRobj.PostProcessing(InputFolder+"/"+InputFile,"tmp/"+InputFile+".GNR.xml");
								}
								
							}
						}
						
						/*
						 * SR & SA
						 */
						if(TrainTest.equals("Test") && (!setup_hash.get("GeneNormalizationOnly").toLowerCase().equals("true")))
						{
							SR SRobj = new SR();
							
							if(Format.equals("PubTator"))
							{
								SRobj.SpeciesRecognition("tmp/"+InputFile,"tmp/"+InputFile+".SR.xml",setup_hash.get("DictionaryFolder")+"/SPStrain.txt",setup_hash.get("FilterAntibody"));
								if(setup_hash.containsKey("GeneSpeciesRecognitionOnly")  && setup_hash.get("GeneSpeciesRecognitionOnly").toLowerCase().equals("true") ) // GeneSpeciesRecognitionOnly
								{
									BioCDocobj.BioC2PubTator(InputFolder+"/"+InputFile,"tmp/"+InputFile+".SR.xml",OutputFolder+"/"+InputFile);
								}
							}
							else
							{
								if(setup_hash.containsKey("GeneSpeciesRecognitionOnly")  && setup_hash.get("GeneSpeciesRecognitionOnly").toLowerCase().equals("true") ) // GeneSpeciesRecognitionOnly
								{
									SRobj.SpeciesRecognition(InputFolder+"/"+InputFile,OutputFolder+"/"+InputFile+".SR.xml",setup_hash.get("DictionaryFolder")+"/SPStrain.txt",setup_hash.get("FilterAntibody"));
								}
								else
								{
									SRobj.SpeciesRecognition(InputFolder+"/"+InputFile,"tmp/"+InputFile+".SR.xml",setup_hash.get("DictionaryFolder")+"/SPStrain.txt",setup_hash.get("FilterAntibody"));
								}
							}
							
							if((!setup_hash.containsKey("GeneSpeciesRecognitionOnly"))  || (!setup_hash.get("GeneSpeciesRecognitionOnly").toLowerCase().equals("true")) )
							{
								if(setup_hash.containsKey("FocusSpecies") && !setup_hash.get("FocusSpecies").equals("All"))
								{
									if(Format.equals("PubTator"))
									{
										SRobj.SpeciesAssignment("tmp/"+InputFile,"tmp/"+InputFile+".SA.xml",setup_hash.get("FocusSpecies"));
									}
									else if(Format.equals("BioC"))
									{
										SRobj.SpeciesAssignment(InputFolder+"/"+InputFile,"tmp/"+InputFile+".SA.xml",setup_hash.get("FocusSpecies"));
									}
								}
								else
								{
									if(Format.equals("PubTator"))
									{
										SRobj.SpeciesAssignment("tmp/"+InputFile,"tmp/"+InputFile+".SA.xml");
									}
									else if(Format.equals("BioC"))
									{
										SRobj.SpeciesAssignment(InputFolder+"/"+InputFile,"tmp/"+InputFile+".SA.xml");
									}
								}
							}
						}
						else
						{
							//GNR.xml copy to SA.xml
							br = new BufferedReader(new FileReader("tmp/"+InputFile+".GNR.xml"));
							BufferedWriter fr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("tmp/"+InputFile+".SA.xml"), "UTF-8"));
							line="";
							while ((line = br.readLine()) != null)  
							{
								fr.write(line);
							}
							br.close();
							fr.close();
						}
						
						
						if(setup_hash.get("SpeciesAssignmentOnly").equals("True"))
						{
							if(Format.equals("PubTator"))
							{
								BioCDocobj.BioC2PubTator(InputFolder+"/"+InputFile,"tmp/"+InputFile+".SA.xml",OutputFolder+"/"+InputFile);
							}
							else
							{
								File fileSA_tmp = new File("tmp/"+InputFile+".SA.xml");
								File fileSA_output = new File(OutputFolder+"/"+InputFile);
								fileSA_tmp.renameTo(fileSA_output);
							}
						}
						else // Normalization
						{	
							if((!setup_hash.containsKey("GeneSpeciesRecognitionOnly"))  || (!setup_hash.get("GeneSpeciesRecognitionOnly").toLowerCase().equals("true")) )
							{
								/*
								 * SimConcept
								 */
								{
									SimConcept SCobj = new SimConcept();
									if(TrainTest.equals("TrainSC"))
									{
										SCobj.FeatureExtraction_Train("tmp/"+InputFile+".SC.data");
										SCobj.CRF_learn(setup_hash.get("SCModel"),"tmp/"+InputFile+".SC.data");
									}
									else if(TrainTest.equals("Test"))
									{
										SCobj.FeatureExtraction_Test("tmp/"+InputFile+".SC.data");
										SCobj.CRF_test(setup_hash.get("SCModel"),"tmp/"+InputFile+".SC.data","tmp/"+InputFile+".SC.output");
										
										if(Format.equals("PubTator"))
										{
											SCobj.ReadCRFresult("tmp/"+InputFile,"tmp/"+InputFile+".SC.output","tmp/"+InputFile+".SC.xml");
										}
										else
										{
											SCobj.ReadCRFresult(InputFolder+"/"+InputFile,"tmp/"+InputFile+".SC.output","tmp/"+InputFile+".SC.xml");
										}
										
									}
								}
								
								/*
								 * GN
								 */
								if(TrainTest.equals("Test"))
								{
									GN GNobj = new GN();
									GNobj.PreProcessing4GN(InputFolder+"/"+InputFile,"tmp/"+InputFile+".PreProcessing4GN.xml");
									GNobj.ChromosomeRecognition(InputFolder+"/"+InputFile,"tmp/"+InputFile+".GN.xml");
									if(setup_hash.containsKey("GeneIDMatch") && setup_hash.get("GeneIDMatch").equals("True"))
									{
										if(Format.equals("PubTator"))
										{
											GNobj.GeneNormalization("tmp/"+InputFile,"tmp/"+InputFile+".GN.xml",true);
											GNobj.GeneIDRecognition("tmp/"+InputFile,"tmp/"+InputFile+".GN.xml");
											BioCDocobj.BioC2PubTator("tmp/"+InputFile+".GN.xml",OutputFolder+"/"+InputFile);
										}
										else if(Format.equals("BioC"))
										{
											GNobj.GeneNormalization(InputFolder+"/"+InputFile,"tmp/"+InputFile+".GN.xml",true);
											GNobj.GeneIDRecognition(InputFolder+"/"+InputFile,OutputFolder+"/"+InputFile);
										}
									}
									else
									{
										if(Format.equals("PubTator"))
										{
											GNobj.GeneNormalization("tmp/"+InputFile,"tmp/"+InputFile+".GN.xml",false);
											BioCDocobj.BioC2PubTator(InputFolder+"/"+InputFile,"tmp/"+InputFile+".GN.xml",OutputFolder+"/"+InputFile);
										}
										else if(Format.equals("BioC"))
										{
											GNobj.GeneNormalization(InputFolder+"/"+InputFile,OutputFolder+"/"+InputFile,false);
										}
									}
								}
							}
						}
						
						/*
						 * remove tmp files
						 */
						if((!setup_hash.containsKey("DeleteTmp")) || setup_hash.get("DeleteTmp").toLowerCase().equals("true"))
						{
							String path="tmp"; 
					        File file = new File(path);
					        File[] files = file.listFiles(); 
					        for (File ftmp:files) 
					        {
					        	if (ftmp.isFile() && ftmp.exists()) 
					            {
					        		if(ftmp.toString().matches("tmp."+InputFile+".*"))
						        	{
					        			ftmp.delete();
						        	}
					        	}
					        }
						}
						
						/*
						 * Time stamp - last
						 */
						endTime = System.currentTimeMillis();
						totTime = endTime - startTime;
						System.out.println(InputFolder+"/"+InputFile+" - ("+Format+" format) : Processing Time:"+totTime/1000+"sec");
					}
				}
			}
		}
	}
}
