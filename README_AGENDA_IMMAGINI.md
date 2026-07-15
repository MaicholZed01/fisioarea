# Fisioarea - Agenda tipo Google Calendar + immagini appuntamento

Questa versione modifica la dashboard trasformando gli appuntamenti in una vera **agenda giornaliera** stile calendario.

## Modifiche principali

- rimossa la tabella classica degli appuntamenti
- aggiunta una vista agenda con fasce orarie dalle 08:00 alle 20:00
- ogni appuntamento appare come una card dentro la sua fascia oraria
- aggiunti pulsanti:
  - Oggi
  - giorno precedente
  - giorno successivo
  - DatePicker
- aggiunta ricerca per paziente, trattamento, stato o sala
- cliccando su una card si apre il dettaglio appuntamento
- è possibile allegare immagini durante la creazione appuntamento
- le immagini vengono copiate e salvate offline nella cartella dati di Fisioarea

## File modificati

```text
src/main/resources/com/fisioarea/view/Dashboard.fxml
src/main/resources/com/fisioarea/view/styles.css
src/main/java/com/fisioarea/controller/DashboardController.java
src/main/java/com/fisioarea/model/Appuntamento.java
src/main/java/com/fisioarea/repository/AppuntamentoRepository.java
src/main/java/com/fisioarea/util/FileStorage.java
src/main/java/com/fisioarea/util/OfflineDataConfig.java
```

## File aggiunto

```text
src/main/java/com/fisioarea/util/ImageStorage.java
```

## Dove vengono salvate le immagini

Le immagini vengono copiate in:

```text
<cartella_utente>/FisioareaData/immagini_appuntamenti
```

Esempio Windows:

```text
C:\Users\NomeUtente\FisioareaData\immagini_appuntamenti
```

Esempio macOS/Linux:

```text
/Users/NomeUtente/FisioareaData/immagini_appuntamenti
```

## File appuntamenti

Il file:

```text
appuntamenti.fisioarea
```

ora salva anche il campo:

```text
immagini
```

Il formato completo è:

```text
id;dataOra;paziente;trattamento;stato;sala;immagini
```

Il progetto resta compatibile anche con i vecchi file appuntamenti senza immagini.

## Avvio

```bash
mvn clean javafx:run
```

## Funzionamento offline

Il software continua a funzionare completamente offline:

```text
FXML → Controller → Service → Repository → File locale
```

Non usa database, server o internet.
