package com.fisioarea.model;

public class User {

    private int id;
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String nomeStudio;
    private String telefono;
    private String ruolo;
    private boolean emailVerified;

    public User() {
    }

    public User(int id, String nome, String cognome, String email, String password,
                String nomeStudio, String telefono, String ruolo) {
        this(id, nome, cognome, email, password, nomeStudio, telefono, ruolo, true);
    }

    public User(int id, String nome, String cognome, String email, String password,
                String nomeStudio, String telefono, String ruolo, boolean emailVerified) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.nomeStudio = nomeStudio;
        this.telefono = telefono;
        this.ruolo = ruolo;
        this.emailVerified = emailVerified;
    }

    public User copy() {
        return new User(
                id,
                nome,
                cognome,
                email,
                password,
                nomeStudio,
                telefono,
                ruolo,
                emailVerified
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

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNomeStudio() {
        return nomeStudio;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getRuolo() {
        return ruolo;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getEmailVerifiedLabel() {
        return emailVerified ? "Verificata" : "Non verificata";
    }

    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNomeStudio(String nomeStudio) {
        this.nomeStudio = nomeStudio;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
