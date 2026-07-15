# Fisioarea - JavaFX MVC Offline con salvataggio su file

Questo progetto è una versione **100% offline** del gestionale Fisioarea.

Non usa database, non usa server, non usa internet.
Tutti i dati vengono salvati su file locali.

## Dove vengono salvati i dati

Alla prima esecuzione viene creata automaticamente questa cartella:

```text
<cartella_utente>/FisioareaData
```

Esempio Windows:

```text
C:\Users\NomeUtente\FisioareaData
```

Esempio macOS/Linux:

```text
/Users/NomeUtente/FisioareaData
```

Dentro vengono creati questi file:

```text
users.fisioarea
pazienti.fisioarea
appuntamenti.fisioarea
```

## Dati salvati

Sono già persistenti su file:

- nuovi utenti creati dalla registrazione
- login degli utenti registrati
- pazienti
- appuntamenti

## Architettura MVC

```text
src/main/java/com/fisioarea/
├── app/
│   └── MainApp.java
├── controller/
│   ├── LoginController.java
│   ├── RegisterController.java
│   └── DashboardController.java
├── model/
│   ├── User.java
│   ├── Paziente.java
│   └── Appuntamento.java
├── repository/
│   ├── UserRepository.java
│   ├── PazienteRepository.java
│   └── AppuntamentoRepository.java
├── service/
│   ├── AuthService.java
│   ├── PazienteService.java
│   └── AppuntamentoService.java
└── util/
    ├── FileStorage.java
    ├── OfflineDataConfig.java
    └── SceneNavigator.java
```

## View FXML

```text
src/main/resources/com/fisioarea/view/
├── Login.fxml
├── Register.fxml
├── Dashboard.fxml
└── styles.css
```

## Come funziona il salvataggio

Il flusso è questo:

```text
FXML
↓
Controller
↓
Service
↓
Repository
↓
File locale
```

Esempio registrazione:

```text
Register.fxml
↓
RegisterController
↓
AuthService
↓
UserRepository
↓
users.fisioarea
```

Esempio appuntamento:

```text
Dashboard.fxml
↓
DashboardController
↓
AppuntamentoService
↓
AppuntamentoRepository
↓
appuntamenti.fisioarea
```

## Avvio

Da terminale, nella cartella del progetto:

```bash
mvn clean javafx:run
```

## Credenziali demo

Alla prima esecuzione viene creato automaticamente un utente demo:

```text
Email: admin@fisioarea.it
Password: admin123
```

## Come aggiungere nuovi dati persistenti

Per aggiungere un nuovo tipo di dato, ad esempio `Terapia`, segui lo stesso schema:

1. crea `model/Terapia.java`
2. crea `repository/TerapiaRepository.java`
3. crea `service/TerapiaService.java`
4. collega il service al controller
5. aggiungi in `OfflineDataConfig.java` il file `terapie.fisioarea`

## Nota importante sulla sicurezza

In questa versione demo la password viene salvata in chiaro per semplicità didattica.
In una versione reale offline, è meglio salvarla con hash sicuro, ad esempio BCrypt o PBKDF2.
