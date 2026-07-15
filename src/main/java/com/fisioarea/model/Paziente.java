package com.fisioarea.model;

import com.fisioarea.util.PatientFileStorage;

import java.util.List;

public class Paziente {

    private int id;
    private String nome;
    private String cognome;
    private String telefono;
    private String email;
    private String indirizzoCasa;
    private String fileAllegati;

    private String eta;
    private String professione;
    private String diagnosi;
    private String forzaMuscolare;
    private String naturaProblema;
    private String esamiStrumentaliInVisione;
    private String core;
    private String anamnesiRemota;
    private String anamnesiProssima;
    private String terapieSvolte;
    private String testSpecificiSomministrati;
    private String misurazionePrestazione;
    private String valutazioneEquilibrio;
    private String valutazioneRomAaromProm;
    private String valutazioneElasticitaMuscolare;
    private String valutazionePosturale;
    private String note;

    public Paziente(int id, String nome, String cognome, String telefono) {
        this(id, nome, cognome, telefono, "", "", "");
    }

    public Paziente(int id, String nome, String cognome, String telefono,
                    String email, String indirizzoCasa, String fileAllegati) {
        this(
                id,
                nome,
                cognome,
                telefono,
                email,
                indirizzoCasa,
                fileAllegati,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
        );
    }

    public Paziente(int id, String nome, String cognome, String telefono,
                    String email, String indirizzoCasa, String fileAllegati,
                    String eta, String professione, String diagnosi, String forzaMuscolare,
                    String naturaProblema, String esamiStrumentaliInVisione, String core,
                    String anamnesiRemota, String anamnesiProssima, String terapieSvolte,
                    String testSpecificiSomministrati, String misurazionePrestazione,
                    String valutazioneEquilibrio, String valutazioneRomAaromProm,
                    String valutazioneElasticitaMuscolare, String valutazionePosturale,
                    String note) {
        this.id = id;
        this.nome = safe(nome);
        this.cognome = safe(cognome);
        this.telefono = safe(telefono);
        this.email = safe(email);
        this.indirizzoCasa = safe(indirizzoCasa);
        this.fileAllegati = safe(fileAllegati);
        this.eta = safe(eta);
        this.professione = safe(professione);
        this.diagnosi = safe(diagnosi);
        this.forzaMuscolare = safe(forzaMuscolare);
        this.naturaProblema = safe(naturaProblema);
        this.esamiStrumentaliInVisione = safe(esamiStrumentaliInVisione);
        this.core = safe(core);
        this.anamnesiRemota = safe(anamnesiRemota);
        this.anamnesiProssima = safe(anamnesiProssima);
        this.terapieSvolte = safe(terapieSvolte);
        this.testSpecificiSomministrati = safe(testSpecificiSomministrati);
        this.misurazionePrestazione = safe(misurazionePrestazione);
        this.valutazioneEquilibrio = safe(valutazioneEquilibrio);
        this.valutazioneRomAaromProm = safe(valutazioneRomAaromProm);
        this.valutazioneElasticitaMuscolare = safe(valutazioneElasticitaMuscolare);
        this.valutazionePosturale = safe(valutazionePosturale);
        this.note = safe(note);
    }

    public Paziente copy() {
        return new Paziente(
                id,
                nome,
                cognome,
                telefono,
                email,
                indirizzoCasa,
                fileAllegati,
                eta,
                professione,
                diagnosi,
                forzaMuscolare,
                naturaProblema,
                esamiStrumentaliInVisione,
                core,
                anamnesiRemota,
                anamnesiProssima,
                terapieSvolte,
                testSpecificiSomministrati,
                misurazionePrestazione,
                valutazioneEquilibrio,
                valutazioneRomAaromProm,
                valutazioneElasticitaMuscolare,
                valutazionePosturale,
                note
        );
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getNomeCompleto() {
        return (nome + " " + cognome).trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public String getIndirizzoCasa() {
        return indirizzoCasa;
    }

    public String getFileAllegati() {
        return fileAllegati;
    }

    public String getEta() {
        return eta;
    }

    public String getProfessione() {
        return professione;
    }

    public String getDiagnosi() {
        return diagnosi;
    }

    public String getForzaMuscolare() {
        return forzaMuscolare;
    }

    public String getNaturaProblema() {
        return naturaProblema;
    }

    public String getEsamiStrumentaliInVisione() {
        return esamiStrumentaliInVisione;
    }

    public String getCore() {
        return core;
    }

    public String getAnamnesiRemota() {
        return anamnesiRemota;
    }

    public String getAnamnesiProssima() {
        return anamnesiProssima;
    }

    public String getTerapieSvolte() {
        return terapieSvolte;
    }

    public String getTestSpecificiSomministrati() {
        return testSpecificiSomministrati;
    }

    public String getMisurazionePrestazione() {
        return misurazionePrestazione;
    }

    public String getValutazioneEquilibrio() {
        return valutazioneEquilibrio;
    }

    public String getValutazioneRomAaromProm() {
        return valutazioneRomAaromProm;
    }

    public String getValutazioneElasticitaMuscolare() {
        return valutazioneElasticitaMuscolare;
    }

    public String getValutazionePosturale() {
        return valutazionePosturale;
    }

    public String getNote() {
        return note;
    }

    public List<String> getListaFileAllegati() {
        return PatientFileStorage.splitFilePaths(fileAllegati);
    }

    public int getNumeroFileAllegati() {
        return getListaFileAllegati().size();
    }

    public String getFileCountLabel() {
        int count = getNumeroFileAllegati();
        return count == 1 ? "1 file" : count + " file";
    }

    public boolean hasFileAllegati() {
        return getNumeroFileAllegati() > 0;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = safe(nome);
    }

    public void setCognome(String cognome) {
        this.cognome = safe(cognome);
    }

    public void setTelefono(String telefono) {
        this.telefono = safe(telefono);
    }

    public void setEmail(String email) {
        this.email = safe(email);
    }

    public void setIndirizzoCasa(String indirizzoCasa) {
        this.indirizzoCasa = safe(indirizzoCasa);
    }

    public void setFileAllegati(String fileAllegati) {
        this.fileAllegati = safe(fileAllegati);
    }

    public void setEta(String eta) {
        this.eta = safe(eta);
    }

    public void setProfessione(String professione) {
        this.professione = safe(professione);
    }

    public void setDiagnosi(String diagnosi) {
        this.diagnosi = safe(diagnosi);
    }

    public void setForzaMuscolare(String forzaMuscolare) {
        this.forzaMuscolare = safe(forzaMuscolare);
    }

    public void setNaturaProblema(String naturaProblema) {
        this.naturaProblema = safe(naturaProblema);
    }

    public void setEsamiStrumentaliInVisione(String esamiStrumentaliInVisione) {
        this.esamiStrumentaliInVisione = safe(esamiStrumentaliInVisione);
    }

    public void setCore(String core) {
        this.core = safe(core);
    }

    public void setAnamnesiRemota(String anamnesiRemota) {
        this.anamnesiRemota = safe(anamnesiRemota);
    }

    public void setAnamnesiProssima(String anamnesiProssima) {
        this.anamnesiProssima = safe(anamnesiProssima);
    }

    public void setTerapieSvolte(String terapieSvolte) {
        this.terapieSvolte = safe(terapieSvolte);
    }

    public void setTestSpecificiSomministrati(String testSpecificiSomministrati) {
        this.testSpecificiSomministrati = safe(testSpecificiSomministrati);
    }

    public void setMisurazionePrestazione(String misurazionePrestazione) {
        this.misurazionePrestazione = safe(misurazionePrestazione);
    }

    public void setValutazioneEquilibrio(String valutazioneEquilibrio) {
        this.valutazioneEquilibrio = safe(valutazioneEquilibrio);
    }

    public void setValutazioneRomAaromProm(String valutazioneRomAaromProm) {
        this.valutazioneRomAaromProm = safe(valutazioneRomAaromProm);
    }

    public void setValutazioneElasticitaMuscolare(String valutazioneElasticitaMuscolare) {
        this.valutazioneElasticitaMuscolare = safe(valutazioneElasticitaMuscolare);
    }

    public void setValutazionePosturale(String valutazionePosturale) {
        this.valutazionePosturale = safe(valutazionePosturale);
    }

    public void setNote(String note) {
        this.note = safe(note);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
