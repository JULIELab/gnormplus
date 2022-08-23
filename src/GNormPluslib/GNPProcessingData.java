package GNormPluslib;

import java.util.HashMap;

public class GNPProcessingData {
    private BioCDoc BioCDocobj = new BioCDoc();
    private HashMap<String, String> Pmid2Abb_hash = new HashMap<String, String>();
    private HashMap<String, String> PmidAbb2LF_lc_hash = new HashMap<String, String>();
    private HashMap<String, String> PmidLF2Abb_lc_hash = new HashMap<String, String>();
    private HashMap<String, String> PmidAbb2LF_hash = new HashMap<String, String>();
    private HashMap<String, String> PmidLF2Abb_hash = new HashMap<String, String>();
    private HashMap<String, String> Pmid2ChromosomeGene_hash = new HashMap<String, String>();
    private HashMap<String, String> SimConceptMention2Type_hash = new HashMap<String, String>();

    public BioCDoc getBioCDocobj() {
        return BioCDocobj;
    }
}
