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

    public HashMap<String, String> getPmid2Abb_hash() {
        return Pmid2Abb_hash;
    }

    public HashMap<String, String> getPmidAbb2LF_lc_hash() {
        return PmidAbb2LF_lc_hash;
    }

    public HashMap<String, String> getPmidLF2Abb_lc_hash() {
        return PmidLF2Abb_lc_hash;
    }

    public HashMap<String, String> getPmidAbb2LF_hash() {
        return PmidAbb2LF_hash;
    }

    public HashMap<String, String> getPmidLF2Abb_hash() {
        return PmidLF2Abb_hash;
    }

    public HashMap<String, String> getPmid2ChromosomeGene_hash() {
        return Pmid2ChromosomeGene_hash;
    }

    public BioCDoc getBioCDocobj() {
        return BioCDocobj;
    }
}
