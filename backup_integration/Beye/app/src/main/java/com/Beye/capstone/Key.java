package com.Beye.capstone;

public class Key {

    private static final String odsayApiKey = new String("O0FYYR/c2rNLlYS75Vj3ifh3HkFk53oDjLgqMTX/nFo");
    private static final String tmapApiKey = new String("957bb8c3-7231-4a23-be9f-2ab03a883098");
    private static final String googleApiKey = new String("AIzaSyBMh4cKj-FhN4ecsipdpYv969aqXGBsXfo");
    private static final String busApiKey = new String("zwbzFqmPh5WnG5YmuH61wOsz0VVaxfwaSMPF%2BL5fUgsdbjf44xP%2FvF2xFAiSHCfD6C2g7REzPYav3yttlr9OeA%3D%3D");

    public Key(){
    }

    public String getBusApiKey() {
        return busApiKey;
    }

    public String getOdsayApiKey() {
        return odsayApiKey;
    }

    public String getTmapApiKey() {
        return tmapApiKey;
    }

    public String getGoogleApiKey() { return googleApiKey; }

}
