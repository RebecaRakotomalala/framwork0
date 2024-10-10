package mg.itu.prom16;

public class VerbMethode {
    String verb;
    String methode;

    public VerbMethode(String verb, String methode) {
        this.verb = verb;
        this.methode = methode;
    }    

    public String getVerb() {
        return verb;
    }
    public void setVerb(String verb) {
        this.verb = verb;
    }
    public String getMethode() {
        return methode;
    }
    public void setMethode(String methode) {
        this.methode = methode;
    }
}
