# Guida al salvataggio offline di Fisioarea

## Obiettivo

Il software deve funzionare solo offline e salvare i dati senza database.

Questa versione salva i dati in file locali dentro:

```text
<cartella_utente>/FisioareaData
```

## File generati

| File | Contenuto |
|---|---|
| `users.fisioarea` | utenti registrati |
| `pazienti.fisioarea` | pazienti |
| `appuntamenti.fisioarea` | appuntamenti |

## Perché non salvo nella cartella `target`

Maven ricrea spesso la cartella `target`.
Se salvassimo i dati lì dentro, rischieresti di perderli quando fai:

```bash
mvn clean
```

Per questo i file vengono salvati nella cartella utente.

## Posso salvare nella cartella del progetto?

Sì. Apri:

```text
src/main/java/com/fisioarea/util/OfflineDataConfig.java
```

e sostituisci:

```java
private static final Path DATA_DIRECTORY = Paths.get(
        System.getProperty("user.home"),
        "FisioareaData"
);
```

con:

```java
private static final Path DATA_DIRECTORY = Paths.get("data");
```

In questo modo i file saranno salvati nella cartella `data` del progetto/applicazione.

## Flusso dati

```text
View FXML → Controller → Service → Repository → File
```

## Cosa modificare per nuove entità

Per salvare una nuova entità, ad esempio `Pagamento`, crea:

```text
model/Pagamento.java
repository/PagamentoRepository.java
service/PagamentoService.java
```

Poi aggiungi in `OfflineDataConfig.java`:

```java
public static Path getPagamentiFile() {
    return DATA_DIRECTORY.resolve("pagamenti.fisioarea");
}
```

Il repository userà `FileStorage.readRows(...)` e `FileStorage.writeRows(...)`.
