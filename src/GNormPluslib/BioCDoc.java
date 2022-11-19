package GNormPluslib;

import com.pengyifan.bioc.*;
import com.pengyifan.bioc.io.BioCCollectionReader;
import com.pengyifan.bioc.io.BioCCollectionWriter;
import com.pengyifan.bioc.io.BioCDocumentWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.ZoneId;
import javax.xml.stream.XMLStreamException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BioCDoc {
	public ArrayList<String> PMIDs = new ArrayList<String>();
	public ArrayList<ArrayList<String>> PassageNames = new ArrayList();
	public ArrayList<ArrayList<Integer>> PassageOffsets = new ArrayList();
	public ArrayList<ArrayList<String>> PassageContexts = new ArrayList();
	public ArrayList<ArrayList<ArrayList<String>>> Annotations = new ArrayList();

	public String BioCFormatCheck(String InputFile) throws IOException {
		try (BioCCollectionReader bioCCollectionReader = new BioCCollectionReader(new InputStreamReader(new FileInputStream(InputFile), "UTF-8"))) {
			BioCCollection collection = bioCCollectionReader.readCollection();
		} catch (UnsupportedEncodingException | FileNotFoundException | XMLStreamException e) {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(InputFile), "UTF-8"));
			String line = "";
			String status = "";
			String Pmid = "";
			boolean tiabs = false;
			Pattern patt = Pattern.compile("^([^\\|\\t]+)\\|([^\\|\\t]+)\\|(.*)$");
			while ((line = br.readLine()) != null) {
				Matcher mat = patt.matcher(line);
				if (mat.find()) {
					if (Pmid.equals("")) {
						Pmid = mat.group(1);
					} else if (!Pmid.equals(mat.group(1))) {
						return "[Error]: " + InputFile + " - A blank is needed between " + Pmid + " and " + mat.group(1) + ".";
					}
					status = "tiabs";
					tiabs = true;
				} else if (line.contains("\t")) {
				} else if (line.length() == 0) {
					if (status.equals("")) {
						if (Pmid.equals("")) {
							return "[Error]: " + InputFile + " - It's neither BioC nor PubTator format. PMID is empty.";
						} else {
							return "[Error]: " + InputFile + " - A redundant blank is after " + Pmid + ".";
						}
					}
					Pmid = "";
					status = "";
				}
			}
			br.close();
			if (tiabs == false) {
				return "[Error]: " + InputFile + " - It's neither BioC nor PubTator format.";
			}
			if (status.equals("")) {
				return "PubTator";
			} else {
				return "[Error]: " + InputFile + " - The last column missed a blank.";
			}
		}
		return "BioC";
	}

	public void PubTator2BioC(String input, String output) throws IOException, XMLStreamException {
		final BioCCollectionWriter bioCCollectionWriter = new BioCCollectionWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
		BioCCollection biocCollection = new BioCCollection();
		ZoneId zonedId = ZoneId.of("America/Montreal");
		LocalDate today = LocalDate.now(zonedId);
		biocCollection.setDate(today.toString());
		biocCollection.setKey("BioC.key");
		biocCollection.setSource("GNormPlus");
		BufferedReader inputfile = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
		ArrayList<String> ParagraphType = new ArrayList<String>();
		ArrayList<String> ParagraphContent = new ArrayList<String>();
		ArrayList<String> annotations = new ArrayList<String>();
		String line;
		String Pmid = "";
		while ((line = inputfile.readLine()) != null) {
			if (line.contains("|") && !line.contains("\t")) {
				String str[] = line.split("\\|", -1);
				Pmid = str[0];
				if (str[1].equals("t")) {
					str[1] = "title";
				}
				if (str[1].equals("a")) {
					str[1] = "abstract";
				}
				ParagraphType.add(str[1]);
				if (str.length == 3) {
					String txt = str[2];
					txt = txt.replaceAll("ω", "w");
					txt = txt.replaceAll("μ", "u");
					txt = txt.replaceAll("κ", "k");
					txt = txt.replaceAll("α", "a");
					txt = txt.replaceAll("γ", "g");
					txt = txt.replaceAll("ɣ", "g");
					txt = txt.replaceAll("β", "b");
					txt = txt.replaceAll("×", "x");
					txt = txt.replaceAll("‑", "-");
					txt = txt.replaceAll("¹", "1");
					txt = txt.replaceAll("²", "2");
					txt = txt.replaceAll("°", "o");
					txt = txt.replaceAll("ö", "o");
					txt = txt.replaceAll("é", "e");
					txt = txt.replaceAll("à", "a");
					txt = txt.replaceAll("Á", "A");
					txt = txt.replaceAll("ε", "e");
					txt = txt.replaceAll("θ", "O");
					txt = txt.replaceAll("•", ".");
					txt = txt.replaceAll("µ", "u");
					txt = txt.replaceAll("λ", "r");
					txt = txt.replaceAll("⁺", "+");
					txt = txt.replaceAll("ν", "v");
					txt = txt.replaceAll("ï", "i");
					txt = txt.replaceAll("ã", "a");
					txt = txt.replaceAll("≡", "=");
					txt = txt.replaceAll("ó", "o");
					txt = txt.replaceAll("³", "3");
					txt = txt.replaceAll("〖", "[");
					txt = txt.replaceAll("〗", "]");
					txt = txt.replaceAll("Å", "A");
					txt = txt.replaceAll("ρ", "p");
					txt = txt.replaceAll("ü", "u");
					txt = txt.replaceAll("ɛ", "e");
					txt = txt.replaceAll("č", "c");
					txt = txt.replaceAll("š", "s");
					txt = txt.replaceAll("ß", "b");
					txt = txt.replaceAll("═", "=");
					txt = txt.replaceAll("£", "L");
					txt = txt.replaceAll("Ł", "L");
					txt = txt.replaceAll("ƒ", "f");
					txt = txt.replaceAll("ä", "a");
					txt = txt.replaceAll("–", "-");
					txt = txt.replaceAll("⁻", "-");
					txt = txt.replaceAll("〈", "<");
					txt = txt.replaceAll("〉", ">");
					txt = txt.replaceAll("χ", "X");
					txt = txt.replaceAll("Đ", "D");
					txt = txt.replaceAll("‰", "%");
					txt = txt.replaceAll("·", ".");
					txt = txt.replaceAll("→", ">");
					txt = txt.replaceAll("←", "<");
					txt = txt.replaceAll("ζ", "z");
					txt = txt.replaceAll("π", "p");
					txt = txt.replaceAll("τ", "t");
					txt = txt.replaceAll("ξ", "X");
					txt = txt.replaceAll("η", "h");
					txt = txt.replaceAll("ø", "0");
					txt = txt.replaceAll("Δ", "D");
					txt = txt.replaceAll("∆", "D");
					txt = txt.replaceAll("∑", "S");
					txt = txt.replaceAll("Ω", "O");
					txt = txt.replaceAll("δ", "d");
					txt = txt.replaceAll("σ", "s");
					txt = txt.replaceAll("Φ", "F");
					txt = txt.replaceAll("[^\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\+\\{\\}\\|\\:\"\\<\\>\\?\\`\\-\\=\\[\\]\\;\\'\\,\\.\\/\\r\\n0-9a-zA-Z ]", " ");
					ParagraphContent.add(txt);
				} else {
					ParagraphContent.add("- No text -");
				}
			} else if (line.contains("\t")) {
				String anno[] = line.split("\t");
				if (anno.length == 6) {
					annotations.add(anno[1] + "\t" + anno[2] + "\t" + anno[3] + "\t" + anno[4] + "\t" + anno[5]);
				} else if (anno.length == 5) {
					annotations.add(anno[1] + "\t" + anno[2] + "\t" + anno[3] + "\t" + anno[4]);
				}
			} else if (line.length() == 0) {
				BioCDocument biocDocument = new BioCDocument();
				biocDocument.setID(Pmid);
				int startoffset = 0;
				for (int i = 0; i < ParagraphType.size(); i++) {
					BioCPassage biocPassage = new BioCPassage();
					Map<String, String> Infons = new HashMap<String, String>();
					Infons.put("type", ParagraphType.get(i));
					biocPassage.setInfons(Infons);
					biocPassage.setText(ParagraphContent.get(i));
					biocPassage.setOffset(startoffset);
					startoffset = startoffset + ParagraphContent.get(i).length() + 1;
					for (int j = 0; j < annotations.size(); j++) {
						String anno[] = annotations.get(j).split("\t");
						if (Integer.parseInt(anno[0]) < startoffset && Integer.parseInt(anno[0]) >= startoffset - ParagraphContent.get(i).length() - 1) {
							BioCAnnotation biocAnnotation = new BioCAnnotation();
							Map<String, String> AnnoInfons = new HashMap<String, String>();
							if (anno.length == 5) {
								AnnoInfons.put("Identifier", anno[4]);
							}
							AnnoInfons.put("type", anno[3]);
							biocAnnotation.setInfons(AnnoInfons);
							BioCLocation location = new BioCLocation(Integer.parseInt(anno[0]), Integer.parseInt(anno[1]) - Integer.parseInt(anno[0]));
							biocAnnotation.setLocations(Set.of(location));
							biocAnnotation.setText(anno[2]);
							biocPassage.addAnnotation(biocAnnotation);
						}
					}
					biocDocument.addPassage(biocPassage);
				}
				biocCollection.addDocument(biocDocument);
				ParagraphType.clear();
				ParagraphContent.clear();
				annotations.clear();
			}
		}
		bioCCollectionWriter.writeCollection(biocCollection);
		bioCCollectionWriter.close();
		inputfile.close();
	}

	public void BioC2PubTator(String input, String output) throws IOException, XMLStreamException {
		HashMap<String, String> pmidlist = new HashMap<String, String>();
		boolean duplicate = false;
		BufferedWriter PubTatorOutputFormat = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
		BioCCollection collection = new BioCCollection();
		final BioCCollectionReader bioCCollectionReader = new BioCCollectionReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
		final Iterator<BioCDocument> docIt = bioCCollectionReader.readCollection().documentIterator();
		while (docIt.hasNext()) {
			BioCDocument document = docIt.next();
			String PMID = document.getID();
			if (pmidlist.containsKey(PMID)) {
				System.out.println("\nError: duplicate pmid-" + PMID);
				duplicate = true;
			} else {
				pmidlist.put(PMID, "");
			}
			String Anno = "";
			for (BioCPassage passage : document.getPassages()) {
				if (passage.getInfon("type").get().equals("title")) {
					PubTatorOutputFormat.write(PMID + "|t|" + passage.getText().get() + "\n");
				} else if (passage.getInfon("type").get().equals("abstract")) {
					PubTatorOutputFormat.write(PMID + "|a|" + passage.getText().get() + "\n");
				} else {
					PubTatorOutputFormat.write(PMID + "|" + passage.getInfon("type").get() + "|" + passage.getText().get() + "\n");
				}
				for (BioCAnnotation annotation : passage.getAnnotations()) {
					String Annotype = annotation.getInfon("type").get();
					String Annoid = "";
					String Proteinid = "";
					if (Annotype.matches("(Gene|FamilyName|DomainMotif)")) {
						if (annotation.getInfons().containsKey("NCBI Gene")) {
							Annoid = annotation.getInfon("NCBI Gene").get();
							String Annoidlist[] = Annoid.split(";");
							Annoid = "";
							for (int x = 0; x < Annoidlist.length; x++) {
								String proteinid = "";
								String homoid = "";
								if (GNormPlus.Normalization2Protein_hash.containsKey(Annoidlist[x])) {
									proteinid = GNormPlus.Normalization2Protein_hash.get(Annoidlist[x]);
								}
								if (GNormPlus.HomologeneID_hash.containsKey(Annoidlist[x])) {
									homoid = GNormPlus.HomologeneID_hash.get(Annoidlist[x]);
								}
								if ((!proteinid.equals("")) || (!homoid.equals(""))) {
									if (Annoid.equals("")) {
										Annoid = Annoidlist[x] + "(";
										if (!proteinid.equals("")) {
											Annoid = Annoid + "UniProt:" + proteinid;
										}
										if (!homoid.equals("")) {
											if (!proteinid.equals("")) {
												Annoid = Annoid + ";";
											}
											Annoid = Annoid + "Homoid:" + homoid;
										}
										Annoid = Annoid + ")";
									} else {
										Annoid = Annoid + ";" + Annoidlist[x] + "(";
										if (!proteinid.equals("")) {
											Annoid = Annoid + "UniProt:" + proteinid;
										}
										if (!homoid.equals("")) {
											if (!proteinid.equals("")) {
												Annoid = Annoid + ";";
											}
											Annoid = Annoid + "Homoid:" + homoid;
										}
										Annoid = Annoid + ")";
									}
								} else {
									if (Annoid.equals("")) {
										Annoid = Annoidlist[x];
									} else {
										Annoid = Annoid + ";" + Annoidlist[x];
									}
								}
							}
						} else {
							Annoid = annotation.getInfon("Identifier").get();
						}
					} else if (Annotype.equals("Species") || Annotype.equals("Genus") || Annotype.equals("Strain")) {
						if (annotation.getInfons().containsKey("NCBI Taxonomy")) {
							Annoid = annotation.getInfon("NCBI Taxonomy").get();
						} else {
							Annoid = annotation.getInfon("Identifier").get();
						}
					} else if (Annotype.equals("CellLine")) {
						if (annotation.getInfons().containsKey("NCBI Taxonomy")) {
							Annoid = annotation.getInfon("NCBI Taxonomy").get();
						} else {
							Annoid = annotation.getInfon("Identifier").get();
						}
					} else {
						Annoid = annotation.getInfon("Identifier").get();
					}
					int start = annotation.getTotalLocation().getOffset();
					int last = start + annotation.getTotalLocation().getLength();
					String AnnoMention = annotation.getText().get();
					Anno = Anno + PMID + "\t" + start + "\t" + last + "\t" + AnnoMention + "\t" + Annotype + "\t" + Annoid + "\n";
				}
			}
			PubTatorOutputFormat.write(Anno + "\n");
		}
		PubTatorOutputFormat.close();
		if (duplicate == true) {
			System.exit(0);
		}
	}

	public void BioC2PubTator(String original_input, String input, String output) throws IOException, XMLStreamException {
		BufferedReader inputfile = new BufferedReader(new InputStreamReader(new FileInputStream(original_input), "UTF-8"));
		HashMap<String, String> ParagraphContent = new HashMap<String, String>();
		HashMap<String, String> annotations = new HashMap<String, String>();
		String line;
		String Pmid = "";
		int count_paragraph = 0;
		while ((line = inputfile.readLine()) != null) {
			if (line.contains("|") && !line.contains("\t")) {
				String str[] = line.split("\\|", -1);
				Pmid = str[0];
				ParagraphContent.put(Pmid + "\t" + str[1], str[2]);
				count_paragraph++;
			} else if (line.contains("\t")) {
				annotations.put(Pmid, annotations.get(Pmid) + line);
			} else if (line.length() == 0) {
				count_paragraph = 0;
			}
		}
		inputfile.close();
		HashMap<String, String> pmidlist = new HashMap<String, String>();
		boolean duplicate = false;
		BufferedWriter PubTatorOutputFormat = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
		BioCCollection collection = new BioCCollection();
		final BioCCollectionReader bioCCollectionReader = new BioCCollectionReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
		final Iterator<BioCDocument> docIt = bioCCollectionReader.readCollection().documentIterator();
		while (docIt.hasNext()) {
			BioCDocument document = docIt.next();
			String PMID = document.getID();
			if (pmidlist.containsKey(PMID)) {
				System.out.println("\nError: duplicate pmid-" + PMID);
				duplicate = true;
			} else {
				pmidlist.put(PMID, "");
			}
			String Anno = "";
			for (BioCPassage passage : document.getPassages()) {
				if (passage.getInfon("type").get().equals("title") || passage.getInfon("type").get().equals("t")) {
					PubTatorOutputFormat.write(PMID + "|t|" + ParagraphContent.get(PMID + "\tt") + "\n");
				} else if (passage.getInfon("type").get().equals("abstract") || passage.getInfon("type").get().equals("a")) {
					PubTatorOutputFormat.write(PMID + "|a|" + ParagraphContent.get(PMID + "\ta") + "\n");
				} else {
					PubTatorOutputFormat.write(PMID + "|" + passage.getInfon("type").get() + "|" + passage.getText().get() + "\n");
				}
				for (BioCAnnotation annotation : passage.getAnnotations()) {
					String Annotype = annotation.getInfon("type").get();
					String Annoid = "";
					String Proteinid = "";
					if (Annotype.matches("(Gene|FamilyName|DomainMotif)")) {
						if (annotation.getInfons().containsKey("NCBI Gene")) {
							Annoid = annotation.getInfon("NCBI Gene").get();
							String Annoidlist[] = Annoid.split(";");
							Annoid = "";
							for (int x = 0; x < Annoidlist.length; x++) {
								String proteinid = "";
								String homoid = "";
								if (GNormPlus.Normalization2Protein_hash.containsKey(Annoidlist[x])) {
									proteinid = GNormPlus.Normalization2Protein_hash.get(Annoidlist[x]);
								}
								if (GNormPlus.HomologeneID_hash.containsKey(Annoidlist[x])) {
									homoid = GNormPlus.HomologeneID_hash.get(Annoidlist[x]);
								}
								if ((!proteinid.equals("")) || (!homoid.equals(""))) {
									if (Annoid.equals("")) {
										Annoid = Annoidlist[x] + "(";
										if (!proteinid.equals("")) {
											Annoid = Annoid + "UniProt:" + proteinid;
										}
										if (!homoid.equals("")) {
											if (!proteinid.equals("")) {
												Annoid = Annoid + ";";
											}
											Annoid = Annoid + "Homoid:" + homoid;
										}
										Annoid = Annoid + ")";
									} else {
										Annoid = Annoid + ";" + Annoidlist[x] + "(";
										if (!proteinid.equals("")) {
											Annoid = Annoid + "UniProt:" + proteinid;
										}
										if (!homoid.equals("")) {
											if (!proteinid.equals("")) {
												Annoid = Annoid + ";";
											}
											Annoid = Annoid + "Homoid:" + homoid;
										}
										Annoid = Annoid + ")";
									}
								} else {
									if (Annoid.equals("")) {
										Annoid = Annoidlist[x];
									} else {
										Annoid = Annoid + ";" + Annoidlist[x];
									}
								}
							}
						} else {
							Annoid = annotation.getInfon("Identifier").get();
						}
					} else if (Annotype.equals("Species") || Annotype.equals("Genus") || Annotype.equals("Strain")) {
						if (annotation.getInfons().containsKey("NCBI Taxonomy")) {
							Annoid = annotation.getInfon("NCBI Taxonomy").get();
						} else {
							Annoid = annotation.getInfon("Identifier").get();
						}
					} else if (Annotype.equals("CellLine")) {
						if (annotation.getInfons().containsKey("NCBI Taxonomy")) {
							Annoid = annotation.getInfon("NCBI Taxonomy").get();
						} else {
							Annoid = annotation.getInfon("Identifier").get();
						}
					} else {
						if (annotation.getInfons().containsKey("Identifier")) {
							Annoid = annotation.getInfon("Identifier").get();
						} else {
							Annoid = "";
						}
					}
					int start = annotation.getTotalLocation().getOffset();
					int last = start + annotation.getTotalLocation().getLength();
					String AnnoMention = annotation.getText().get();
					if (Annoid != null && !Annoid.equals(null)) {
						Anno = Anno + PMID + "\t" + start + "\t" + last + "\t" + AnnoMention + "\t" + Annotype + "\t" + Annoid + "\n";
					} else {
						Anno = Anno + PMID + "\t" + start + "\t" + last + "\t" + AnnoMention + "\t" + Annotype + "\n";
					}
				}
			}
			PubTatorOutputFormat.write(Anno + "\n");
		}
		PubTatorOutputFormat.close();
		if (duplicate == true) {
			System.exit(0);
		}
	}

	public void BioCReader(String input) throws IOException, XMLStreamException {
		BioCCollection collection = new BioCCollection();
		final BioCCollectionReader bioCCollectionReader = new BioCCollectionReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
		final Iterator<BioCDocument> docIt = bioCCollectionReader.readCollection().documentIterator();
		while (docIt.hasNext()) {
			BioCDocument document = docIt.next();
			PMIDs.add(document.getID());
			ArrayList<String> PassageName = new ArrayList<String>();
			ArrayList<Integer> PassageOffset = new ArrayList<Integer>();
			ArrayList<String> PassageContext = new ArrayList<String>();
			ArrayList<ArrayList<String>> AnnotationInPMID = new ArrayList();
			for (BioCPassage passage : document.getPassages()) {
				PassageName.add(passage.getInfon("type").get());
				String txt = passage.getText().get();
				if (txt.matches("[\t ]+")) {
					txt = txt.replaceAll(".", "@");
				} else {
					txt = txt.replaceAll("ω", "w");
					txt = txt.replaceAll("μ", "u");
					txt = txt.replaceAll("κ", "k");
					txt = txt.replaceAll("α", "a");
					txt = txt.replaceAll("γ", "g");
					txt = txt.replaceAll("ɣ", "g");
					txt = txt.replaceAll("β", "b");
					txt = txt.replaceAll("×", "x");
					txt = txt.replaceAll("‑", "-");
					txt = txt.replaceAll("¹", "1");
					txt = txt.replaceAll("²", "2");
					txt = txt.replaceAll("°", "o");
					txt = txt.replaceAll("ö", "o");
					txt = txt.replaceAll("é", "e");
					txt = txt.replaceAll("à", "a");
					txt = txt.replaceAll("Á", "A");
					txt = txt.replaceAll("ε", "e");
					txt = txt.replaceAll("θ", "O");
					txt = txt.replaceAll("•", ".");
					txt = txt.replaceAll("µ", "u");
					txt = txt.replaceAll("λ", "r");
					txt = txt.replaceAll("⁺", "+");
					txt = txt.replaceAll("ν", "v");
					txt = txt.replaceAll("ï", "i");
					txt = txt.replaceAll("ã", "a");
					txt = txt.replaceAll("≡", "=");
					txt = txt.replaceAll("ó", "o");
					txt = txt.replaceAll("³", "3");
					txt = txt.replaceAll("〖", "[");
					txt = txt.replaceAll("〗", "]");
					txt = txt.replaceAll("Å", "A");
					txt = txt.replaceAll("ρ", "p");
					txt = txt.replaceAll("ü", "u");
					txt = txt.replaceAll("ɛ", "e");
					txt = txt.replaceAll("č", "c");
					txt = txt.replaceAll("š", "s");
					txt = txt.replaceAll("ß", "b");
					txt = txt.replaceAll("═", "=");
					txt = txt.replaceAll("£", "L");
					txt = txt.replaceAll("Ł", "L");
					txt = txt.replaceAll("ƒ", "f");
					txt = txt.replaceAll("ä", "a");
					txt = txt.replaceAll("–", "-");
					txt = txt.replaceAll("⁻", "-");
					txt = txt.replaceAll("〈", "<");
					txt = txt.replaceAll("〉", ">");
					txt = txt.replaceAll("χ", "X");
					txt = txt.replaceAll("Đ", "D");
					txt = txt.replaceAll("‰", "%");
					txt = txt.replaceAll("·", ".");
					txt = txt.replaceAll("→", ">");
					txt = txt.replaceAll("←", "<");
					txt = txt.replaceAll("ζ", "z");
					txt = txt.replaceAll("π", "p");
					txt = txt.replaceAll("τ", "t");
					txt = txt.replaceAll("ξ", "X");
					txt = txt.replaceAll("η", "h");
					txt = txt.replaceAll("ø", "0");
					txt = txt.replaceAll("Δ", "D");
					txt = txt.replaceAll("∆", "D");
					txt = txt.replaceAll("∑", "S");
					txt = txt.replaceAll("Ω", "O");
					txt = txt.replaceAll("δ", "d");
					txt = txt.replaceAll("σ", "s");
					txt = txt.replaceAll("Φ", "F");
				}
				if (passage.getText().get().equals("") || passage.getText().get().matches("[ ]+")) {
					PassageContext.add("-notext-");
				} else {
					PassageContext.add(txt);
				}
				PassageOffset.add(passage.getOffset());
				ArrayList<String> AnnotationInPassage = new ArrayList<String>();
				AnnotationInPMID.add(AnnotationInPassage);
			}
			PassageNames.add(PassageName);
			PassageContexts.add(PassageContext);
			PassageOffsets.add(PassageOffset);
			Annotations.add(AnnotationInPMID);
		}
		System.out.println("[BioCReader] PMCIDs.size(): " + PMIDs.size() + ", " + PMIDs);
	}

	public void BioCReaderWithAnnotation(String input) throws IOException, XMLStreamException {
		BioCCollection collection = new BioCCollection();
		final BioCCollectionReader bioCCollectionReader = new BioCCollectionReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
		final Iterator<BioCDocument> docIt = bioCCollectionReader.readCollection().documentIterator();
		while (docIt.hasNext()) {
			BioCDocument document = docIt.next();
			PMIDs.add(document.getID());
			ArrayList<String> PassageName = new ArrayList<String>();
			ArrayList<Integer> PassageOffset = new ArrayList<Integer>();
			ArrayList<String> PassageContext = new ArrayList<String>();
			ArrayList<ArrayList<String>> AnnotationInPMID = new ArrayList();
			for (BioCPassage passage : document.getPassages()) {
				PassageName.add(passage.getInfon("type").get());
				String txt = passage.getText().get();
				if (txt.matches("[\t ]+")) {
					txt = txt.replaceAll(".", "@");
				} else {
					txt = txt.replaceAll("ω", "w");
					txt = txt.replaceAll("μ", "u");
					txt = txt.replaceAll("κ", "k");
					txt = txt.replaceAll("α", "a");
					txt = txt.replaceAll("γ", "g");
					txt = txt.replaceAll("ɣ", "g");
					txt = txt.replaceAll("β", "b");
					txt = txt.replaceAll("×", "x");
					txt = txt.replaceAll("‑", "-");
					txt = txt.replaceAll("¹", "1");
					txt = txt.replaceAll("²", "2");
					txt = txt.replaceAll("°", "o");
					txt = txt.replaceAll("ö", "o");
					txt = txt.replaceAll("é", "e");
					txt = txt.replaceAll("à", "a");
					txt = txt.replaceAll("Á", "A");
					txt = txt.replaceAll("ε", "e");
					txt = txt.replaceAll("θ", "O");
					txt = txt.replaceAll("•", ".");
					txt = txt.replaceAll("µ", "u");
					txt = txt.replaceAll("λ", "r");
					txt = txt.replaceAll("⁺", "+");
					txt = txt.replaceAll("ν", "v");
					txt = txt.replaceAll("ï", "i");
					txt = txt.replaceAll("ã", "a");
					txt = txt.replaceAll("≡", "=");
					txt = txt.replaceAll("ó", "o");
					txt = txt.replaceAll("³", "3");
					txt = txt.replaceAll("〖", "[");
					txt = txt.replaceAll("〗", "]");
					txt = txt.replaceAll("Å", "A");
					txt = txt.replaceAll("ρ", "p");
					txt = txt.replaceAll("ü", "u");
					txt = txt.replaceAll("ɛ", "e");
					txt = txt.replaceAll("č", "c");
					txt = txt.replaceAll("š", "s");
					txt = txt.replaceAll("ß", "b");
					txt = txt.replaceAll("═", "=");
					txt = txt.replaceAll("£", "L");
					txt = txt.replaceAll("Ł", "L");
					txt = txt.replaceAll("ƒ", "f");
					txt = txt.replaceAll("ä", "a");
					txt = txt.replaceAll("–", "-");
					txt = txt.replaceAll("⁻", "-");
					txt = txt.replaceAll("〈", "<");
					txt = txt.replaceAll("〉", ">");
					txt = txt.replaceAll("χ", "X");
					txt = txt.replaceAll("Đ", "D");
					txt = txt.replaceAll("‰", "%");
					txt = txt.replaceAll("·", ".");
					txt = txt.replaceAll("→", ">");
					txt = txt.replaceAll("←", "<");
					txt = txt.replaceAll("ζ", "z");
					txt = txt.replaceAll("π", "p");
					txt = txt.replaceAll("τ", "t");
					txt = txt.replaceAll("ξ", "X");
					txt = txt.replaceAll("η", "h");
					txt = txt.replaceAll("ø", "0");
					txt = txt.replaceAll("Δ", "D");
					txt = txt.replaceAll("∆", "D");
					txt = txt.replaceAll("∑", "S");
					txt = txt.replaceAll("Ω", "O");
					txt = txt.replaceAll("δ", "d");
					txt = txt.replaceAll("σ", "s");
					txt = txt.replaceAll("Φ", "F");
				}
				if (passage.getText().get().equals("") || passage.getText().get().matches("[ ]+")) {
					PassageContext.add("-notext-");
				} else {
					PassageContext.add(txt);
				}
				PassageOffset.add(passage.getOffset());
				ArrayList<String> AnnotationInPassage = new ArrayList<String>();
				for (BioCAnnotation Anno : passage.getAnnotations()) {
					int start = Anno.getTotalLocation().getOffset() - passage.getOffset();
					int last = start + Anno.getTotalLocation().getLength();
					String AnnoMention = Anno.getText().get();
					String Annotype = Anno.getInfon("type").get();
					String Annoid = Anno.getInfon("Identifier").orElse(null);
					if (Annoid == null) {
						Annoid = Anno.getInfon("Identifier").orElse(null);
					}
					if (Annoid == null || Annoid.equals("null")) {
						AnnotationInPassage.add(start + "\t" + last + "\t" + AnnoMention + "\t" + Annotype);
					} else {
						AnnotationInPassage.add(start + "\t" + last + "\t" + AnnoMention + "\t" + Annotype + "\t" + Annoid);
					}
				}
				AnnotationInPMID.add(AnnotationInPassage);
			}
			PassageNames.add(PassageName);
			PassageContexts.add(PassageContext);
			PassageOffsets.add(PassageOffset);
			Annotations.add(AnnotationInPMID);
		}
		System.out.println("[BioCReaderWithAnnotation] PMCIDs.size(): " + PMIDs.size()+ ", " + PMIDs + "; Annotations: " + Annotations.size());

	}

	public void BioCOutput(String input, String output, ArrayList<ArrayList<ArrayList<String>>> Annotations, boolean Final, boolean RemovePreviousAnno) throws IOException, XMLStreamException {
		boolean ShowUnNormalizedMention = false;
		if (GNormPlus.setup_hash.containsKey("ShowUnNormalizedMention") && GNormPlus.setup_hash.get("ShowUnNormalizedMention").equals("True")) {
			ShowUnNormalizedMention = true;
		}
		final BioCDocumentWriter BioCOutputFormat = new BioCDocumentWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
		BioCCollection biocCollection_input = new BioCCollection();
		BioCCollection biocCollection_output = new BioCCollection();
		biocCollection_input = new BioCCollectionReader(new InputStreamReader(new FileInputStream(input), "UTF-8")).readCollection();
		BioCOutputFormat.writeBeginCollectionInfo(biocCollection_input);
		int i = 0;
		final Iterator<BioCDocument> docIt = biocCollection_input.documentIterator();
		while (docIt.hasNext()) {
			BioCDocument document_output = new BioCDocument();
			final BioCDocument document_input = docIt.next();
			String PMID = document_input.getID();
			document_output.setID(PMID);
			int annotation_count = 0;
			int j = 0;
			for (BioCPassage passage_input : document_input.getPassages()) {
				BioCPassage passage_output = passage_input;
				if (RemovePreviousAnno == true) {
					passage_output.clearAnnotations();
				} else {
					for (BioCAnnotation annotation : passage_output.getAnnotations()) {
						annotation.setID("" + annotation_count);
						annotation_count++;
					}
				}
				int passage_Offset = passage_input.getOffset();
				String passage_Text = passage_input.getText().get();
				ArrayList<String> AnnotationInPassage = new ArrayList<String>();
				if (Annotations.size() > i && Annotations.get(i).size() > j) {
					for (int a = 0; a < Annotations.get(i).get(j).size(); a++) {
						String Anno[] = Annotations.get(i).get(j).get(a).split("\\t");
						int start = Integer.parseInt(Anno[0]);
						int last = Integer.parseInt(Anno[1]);
						boolean found = false;
						if (passage_Text.length() > last) {
							String mention = Anno[2];
							if (Final == true && passage_Text.length() >= last) {
								mention = passage_Text.substring(start, last);
							}
							if (mention.matches(".*\t.*")) {
								Anno[3] = Anno[4];
								if (Anno.length >= 6) {
									Anno[4] = Anno[5];
								}
							}
							String type = Anno[3];
							String id = "";
							if (Anno.length >= 5) {
								id = Anno[4];
							}
							if (Final == true) {
								for (int b = 0; b < AnnotationInPassage.size(); b++) {
									String Annob[] = AnnotationInPassage.get(b).split("\\t");
									int startb = Integer.parseInt(Annob[0]);
									int lastb = Integer.parseInt(Annob[1]);
									String mentionb = Annob[2];
									if (Final == true && passage_Text.length() >= lastb) {
										mentionb = passage_Text.substring(startb, lastb);
									}
									if (mentionb.matches(".*\t.*")) {
										Annob[3] = Annob[4];
										if (Annob.length >= 6) {
											Annob[4] = Annob[5];
										}
									}
									String typeb = Annob[3];
									String idb = "";
									if (Annob.length >= 5) {
										idb = Annob[4];
									}
									if (start == startb && last == lastb && type.equals(typeb)) {
										found = true;
										if (id.matches("(Focus|Right|Left|Prefix|GeneID|Tax):[0-9]+") && (!idb.equals(""))) {
										} else if (idb.matches("(Focus|Right|Left|Prefix|GeneID|Tax):[0-9]+") && (!id.matches("(Focus|Right|Left|Prefix|GeneID|Tax):[0-9]+")) && (!id.equals(""))) {
											AnnotationInPassage.set(b, start + "\t" + last + "\t" + mention + "\t" + type + "\t" + id);
										} else {
											if (id.equals("")) {
											} else {
												AnnotationInPassage.set(b, start + "\t" + last + "\t" + mention + "\t" + type + "\t" + idb + ";" + id);
											}
										}
										break;
									}
								}
							}
						}
						if (found == false) {
							AnnotationInPassage.add(Annotations.get(i).get(j).get(a));
						}
					}
				}
				for (int a = 0; a < AnnotationInPassage.size(); a++) {
					String Anno[] = AnnotationInPassage.get(a).split("\\t");
					HashMap<String, String> id_hash = new HashMap<String, String>();
					if (Anno.length >= 5) {
						int start = Integer.parseInt(Anno[0]);
						int last = Integer.parseInt(Anno[1]);
						String mention = Anno[2];
						if (Final == true && passage_Text.length() >= last) {
							mention = passage_Text.substring(start, last);
						}
						if (mention.matches(".*\t.*")) {
							Anno[3] = Anno[4];
							if (Anno.length >= 6) {
								Anno[4] = Anno[5];
							}
						}
						String ids = Anno[4];
						String idlist[] = ids.split(",");
						for (int b = 0; b < idlist.length; b++) {
							id_hash.put(idlist[b], "");
						}
						ids = "";
						for (String id : id_hash.keySet()) {
							if (ids.equals("")) {
								ids = id;
							} else {
								ids = ids + ";" + id;
							}
						}
						AnnotationInPassage.set(a, Anno[0] + "\t" + Anno[1] + "\t" + Anno[2] + "\t" + Anno[3] + "\t" + ids);
					}
				}
				for (int a = 0; a < AnnotationInPassage.size(); a++) {
					String Anno[] = AnnotationInPassage.get(a).split("\\t");
					int start = Integer.parseInt(Anno[0]);
					int last = Integer.parseInt(Anno[1]);
					if (passage_Text.length() > last) {
						String mention = Anno[2];
						if (Final == true && passage_Text.length() >= last) {
							mention = passage_Text.substring(start, last);
						}
						if (mention.matches(".*\t.*")) {
							Anno[3] = Anno[4];
							if (Anno.length >= 6) {
								Anno[4] = Anno[5];
							}
						}
						if (Anno.length < 4)
							throw new IllegalStateException("Document with ID " + PMID + " has annotation \"" + Arrays.toString(Anno) + "\" which is too short: A minimal length of 4 is expected.");
						String type = Anno[3];
						if (type.equals("GeneID")) {
							type = "Gene";
						}
						BioCAnnotation biocAnnotation = new BioCAnnotation();
						Map<String, String> AnnoInfons = new HashMap<String, String>();
						AnnoInfons.put("type", type);
						if (Anno.length >= 5) {
							String identifier = Anno[4];
							if (Final == true && ShowUnNormalizedMention == false) {
								if (type.matches("(FamilyName|Domain|Gene)")) {
									Pattern ptmp0 = Pattern.compile("^(Focus|Right|Left|Prefix|GeneID|Tax)\\:([0-9]+)\\|([0-9\\;]+)$");
									Matcher mtmp0 = ptmp0.matcher(identifier);
									Pattern ptmp1 = Pattern.compile("^(Focus|Right|Left|Prefix|GeneID|Tax)\\:([0-9]+)\\|([0-9]+)\\-([0-9]+)$");
									Matcher mtmp1 = ptmp1.matcher(identifier);
									Pattern ptmp2 = Pattern.compile("^(Focus|Right|Left|Prefix|GeneID|Tax)\\:([0-9]+)$");
									Matcher mtmp2 = ptmp2.matcher(identifier);
									Pattern ptmp3 = Pattern.compile("^Homo\\:([0-9]+)$");
									Matcher mtmp3 = ptmp3.matcher(identifier);
									if (mtmp0.find()) {
										String Method_SA = mtmp0.group(1);
										String TaxonomyID = mtmp0.group(2);
										String NCBIGeneID = mtmp0.group(3);
										if (GNormPlus.Normalization2Protein_hash.containsKey(NCBIGeneID)) {
											AnnoInfons.put("UniProt", GNormPlus.Normalization2Protein_hash.get(NCBIGeneID));
										}
										if (GNormPlus.HomologeneID_hash.containsKey(NCBIGeneID)) {
											AnnoInfons.put("NCBI Homologene", GNormPlus.HomologeneID_hash.get(NCBIGeneID));
										}
										AnnoInfons.put("NCBI Gene", NCBIGeneID);
									} else if (mtmp1.find()) {
										String Method_SA = mtmp1.group(1);
										String TaxonomyID = mtmp1.group(2);
										String NCBIGeneID = mtmp1.group(3);
										String HomoID = mtmp1.group(4);
										if (GNormPlus.Normalization2Protein_hash.containsKey(NCBIGeneID)) {
											AnnoInfons.put("UniProt", GNormPlus.Normalization2Protein_hash.get(NCBIGeneID));
										}
										if (GNormPlus.HomologeneID_hash.containsKey(NCBIGeneID)) {
											AnnoInfons.put("NCBI Homologene", GNormPlus.HomologeneID_hash.get(NCBIGeneID));
										}
										AnnoInfons.put("NCBI Gene", NCBIGeneID);
									} else if (mtmp2.find()) {
										String Method_SA = mtmp2.group(1);
										String TaxonomyID = mtmp2.group(2);
										AnnoInfons.put("FocusSpecies", "NCBITaxonomyID:" + TaxonomyID);
									} else if (mtmp3.find()) {
										String Method_SA = mtmp3.group(1);
										String HomoID = mtmp3.group(2);
										AnnoInfons.put("NCBI Homologene", HomoID);
									} else {
										String identifiers[] = identifier.split(";");
										if (identifiers.length > 1) {
											ArrayList<String> identifierSTR = new ArrayList<String>();
											ArrayList<String> ProteinidSTR = new ArrayList<String>();
											ArrayList<String> HomoidSTR = new ArrayList<String>();
											for (int idi = 0; idi < identifiers.length; idi++) {
												Pattern ptmp4 = Pattern.compile("^(Focus|Right|Left|Prefix|GeneID|Tax)\\:([0-9]+)\\|([0-9]+)\\-([0-9]+)$");
												Matcher mtmp4 = ptmp4.matcher(identifiers[idi]);
												Pattern ptmp5 = Pattern.compile("^(Focus|Right|Left|Prefix|GeneID|Tax)\\:([0-9]+)\\|([0-9\\;]+)$");
												Matcher mtmp5 = ptmp5.matcher(identifiers[idi]);
												if (mtmp4.find()) {
													String Method_SA = mtmp4.group(1);
													String TaxonomyID = mtmp4.group(2);
													String NCBIGeneID = mtmp4.group(3);
													String HomoID = mtmp4.group(4);
													if (!identifierSTR.contains(NCBIGeneID)) {
														identifierSTR.add(NCBIGeneID);
													}
													if (GNormPlus.Normalization2Protein_hash.containsKey(NCBIGeneID)) {
														if (!ProteinidSTR.contains(GNormPlus.Normalization2Protein_hash.containsKey(NCBIGeneID))) {
															ProteinidSTR.add(GNormPlus.Normalization2Protein_hash.get(NCBIGeneID));
														}
													}
													if (GNormPlus.HomologeneID_hash.containsKey(NCBIGeneID)) {
														if (!HomoidSTR.contains(GNormPlus.HomologeneID_hash.containsKey(NCBIGeneID))) {
															HomoidSTR.add(GNormPlus.HomologeneID_hash.get(NCBIGeneID));
														}
													}
												} else if (mtmp5.find()) {
													String Method_SA = mtmp5.group(1);
													String TaxonomyID = mtmp5.group(2);
													String NCBIGeneID = mtmp5.group(3);
													if (!identifierSTR.contains(NCBIGeneID)) {
														identifierSTR.add(NCBIGeneID);
													}
												}
											}
											String idSTR = "";
											for (int x = 0; x < identifierSTR.size(); x++) {
												if (idSTR.equals("")) {
													idSTR = identifierSTR.get(x);
												} else {
													idSTR = idSTR + ";" + identifierSTR.get(x);
												}
											}
											AnnoInfons.put("NCBI Gene", idSTR);
											String pidSTR = "";
											for (int x = 0; x < ProteinidSTR.size(); x++) {
												if (pidSTR.equals("")) {
													pidSTR = ProteinidSTR.get(x);
												} else {
													pidSTR = pidSTR + ";" + ProteinidSTR.get(x);
												}
											}
											if (!pidSTR.equals("")) {
												AnnoInfons.put("UniProt", pidSTR);
											}
											String hidSTR = "";
											for (int x = 0; x < HomoidSTR.size(); x++) {
												if (hidSTR.equals("")) {
													hidSTR = HomoidSTR.get(x);
												} else {
													hidSTR = hidSTR + ";" + HomoidSTR.get(x);
												}
											}
											if (!hidSTR.equals("")) {
												AnnoInfons.put("NCBI Homologene", hidSTR);
											}
										}
									}
								} else if (type.matches("(Species|Genus|Strain)")) {
									AnnoInfons.put("type", type);
									AnnoInfons.put("NCBI Taxonomy", identifier);
								} else if (type.matches("Cell")) {
									AnnoInfons.put("type", "CellLine");
									AnnoInfons.put("NCBI Taxonomy", identifier);
								} else {
									AnnoInfons.put("Identifier", identifier);
								}
							} else {
								AnnoInfons.put("Identifier", identifier);
							}
						}
						biocAnnotation.setInfons(AnnoInfons);
						BioCLocation location = new BioCLocation(start + passage_Offset, last - start);
						biocAnnotation.setLocations(Set.of(location));
						biocAnnotation.setText(mention);
						biocAnnotation.setID("" + annotation_count);
						annotation_count++;
						if (Final == true) {
							// "FamilyName" added by Erik Faessler, JULIE Lab: Output FamilyNames
							if (AnnoInfons.containsKey("Identifier") || AnnoInfons.containsKey("NCBI Homologene") || AnnoInfons.containsKey("NCBI Gene") || AnnoInfons.containsKey("NCBI Taxonomy") || type.equals("FamilyName")) {
								passage_output.addAnnotation(biocAnnotation);
							}
						} else {
							passage_output.addAnnotation(biocAnnotation);
						}
					}
				}
				document_output.addPassage(passage_output);
				j++;
			}
			biocCollection_output.addDocument(document_output);
			BioCOutputFormat.writeDocument(document_output);
			i++;
		}
		BioCOutputFormat.close();
	}
}
