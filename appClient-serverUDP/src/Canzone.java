import java.io.File;

public class Canzone {
    private String titolo;

    public Canzone(String titolo) {
        this.titolo = titolo;
    }

    public String getNomeFileCompleto() {
        return titolo + ".mp3";
    }
}