# Fisioarea - JavaFX MVC Offline UI pulita

Questa versione contiene le modifiche richieste:

- Login senza `StackPane` esterno
- Registrazione senza `StackPane` esterno
- Dashboard semplificata
- rimossi dalla sidebar:
  - Terapie
  - Pagamenti
  - Report
- rimosso il blocco Azioni rapide
- rimosse le card:
  - Appuntamenti oggi
  - Pazienti attivi
  - Sedute completate
  - Fatturato mese

## File modificati

```text
src/main/resources/com/fisioarea/view/Login.fxml
src/main/resources/com/fisioarea/view/Register.fxml
src/main/resources/com/fisioarea/view/Dashboard.fxml
src/main/resources/com/fisioarea/view/styles.css
src/main/java/com/fisioarea/controller/DashboardController.java
```

## Avvio

```bash
mvn clean javafx:run
```

## Salvataggio offline

I dati continuano a essere salvati su file locali nella cartella:

```text
<cartella_utente>/FisioareaData
```

File usati:

```text
users.fisioarea
pazienti.fisioarea
appuntamenti.fisioarea
```

## Credenziali demo

```text
Email: admin@fisioarea.it
Password: admin123
```
