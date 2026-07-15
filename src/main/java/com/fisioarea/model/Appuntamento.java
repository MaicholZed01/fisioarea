package com.fisioarea.model;

import com.fisioarea.util.ImageStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Appuntamento {

    private int id;
    private LocalDateTime dataOra;
    private String paziente;
    private String trattamento;
    private String stato;
    private String sala;
    private String immagini;

    public Appuntamento(int id, LocalDateTime dataOra, String paziente, String trattamento, String stato, String sala) {
        this(id, dataOra, paziente, trattamento, stato, sala, "");
    }

    public Appuntamento(int id, LocalDateTime dataOra, String paziente, String trattamento,
                        String stato, String sala, String immagini) {
        this.id = id;
        this.dataOra = dataOra;
        this.paziente = paziente;
        this.trattamento = trattamento;
        this.stato = stato;
        this.sala = sala;
        this.immagini = immagini == null ? "" : immagini;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public String getOra() {
        return dataOra.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getPaziente() {
        return paziente;
    }

    public String getTrattamento() {
        return trattamento;
    }

    public String getStato() {
        return stato;
    }

    public String getSala() {
        return sala;
    }

    public String getImmagini() {
        return immagini;
    }

    public List<String> getListaImmagini() {
        return ImageStorage.splitImagePaths(immagini);
    }

    public int getNumeroImmagini() {
        return getListaImmagini().size();
    }

    public boolean hasImmagini() {
        return getNumeroImmagini() > 0;
    }
}
