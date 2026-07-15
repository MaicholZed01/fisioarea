package com.fisioarea.service;

import com.fisioarea.model.Paziente;
import com.fisioarea.repository.PazienteRepository;
import com.fisioarea.util.PatientFileStorage;
import com.fisioarea.util.ValidationUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PazienteService {

    private final PazienteRepository pazienteRepository = new PazienteRepository();

    public List<Paziente> getPazienti() {
        return pazienteRepository.findAll();
    }

    public ServiceResult<Paziente> creaPaziente(PatientClinicalData data, List<File> filesToAttach) {
        ServiceResult<Paziente> validation = validatePatientFields(data);

        if (!validation.isSuccess()) {
            return validation;
        }

        Paziente paziente = createPatientFromData(0, "", data);
        Paziente savedPatient = pazienteRepository.save(paziente);

        List<String> savedFiles = PatientFileStorage.copyPatientFiles(savedPatient.getId(), filesToAttach);
        savedPatient.setFileAllegati(PatientFileStorage.joinFilePaths(savedFiles));

        savedPatient = pazienteRepository.update(savedPatient);

        return ServiceResult.success("Paziente creato correttamente.", savedPatient);
    }

    public ServiceResult<Paziente> aggiornaPaziente(Paziente pazienteEsistente,
                                                    PatientClinicalData data,
                                                    List<File> filesToAttach) {
        if (pazienteEsistente == null || pazienteEsistente.getId() <= 0) {
            return ServiceResult.failure("Paziente non valido.");
        }

        ServiceResult<Paziente> validation = validatePatientFields(data);

        if (!validation.isSuccess()) {
            return validation;
        }

        List<String> filePaths = new ArrayList<>(pazienteEsistente.getListaFileAllegati());
        filePaths.addAll(PatientFileStorage.copyPatientFiles(pazienteEsistente.getId(), filesToAttach));

        Paziente updatedPatient = createPatientFromData(
                pazienteEsistente.getId(),
                PatientFileStorage.joinFilePaths(filePaths),
                data
        );

        updatedPatient = pazienteRepository.update(updatedPatient);

        return ServiceResult.success("Paziente aggiornato correttamente.", updatedPatient);
    }

    public ServiceResult<Paziente> creaPaziente(String nome, String cognome, String telefono,
                                                String email, String indirizzoCasa,
                                                List<File> filesToAttach) {
        return creaPaziente(
                new PatientClinicalData(
                        nome, cognome, telefono, email, indirizzoCasa,
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
                ),
                filesToAttach
        );
    }

    public ServiceResult<Paziente> aggiornaPaziente(Paziente pazienteEsistente,
                                                    String nome, String cognome, String telefono,
                                                    String email, String indirizzoCasa,
                                                    List<File> filesToAttach) {
        PatientClinicalData data = new PatientClinicalData(
                nome, cognome, telefono, email, indirizzoCasa,
                pazienteEsistente.getEta(),
                pazienteEsistente.getProfessione(),
                pazienteEsistente.getDiagnosi(),
                pazienteEsistente.getForzaMuscolare(),
                pazienteEsistente.getNaturaProblema(),
                pazienteEsistente.getEsamiStrumentaliInVisione(),
                pazienteEsistente.getCore(),
                pazienteEsistente.getAnamnesiRemota(),
                pazienteEsistente.getAnamnesiProssima(),
                pazienteEsistente.getTerapieSvolte(),
                pazienteEsistente.getTestSpecificiSomministrati(),
                pazienteEsistente.getMisurazionePrestazione(),
                pazienteEsistente.getValutazioneEquilibrio(),
                pazienteEsistente.getValutazioneRomAaromProm(),
                pazienteEsistente.getValutazioneElasticitaMuscolare(),
                pazienteEsistente.getValutazionePosturale(),
                pazienteEsistente.getNote()
        );

        return aggiornaPaziente(pazienteEsistente, data, filesToAttach);
    }

    public void eliminaPaziente(int id) {
        pazienteRepository.deleteById(id);
    }

    private Paziente createPatientFromData(int id, String fileAllegati, PatientClinicalData data) {
        return new Paziente(
                id,
                clean(data.nome()),
                clean(data.cognome()),
                clean(data.telefono()),
                ValidationUtil.normalizeEmail(data.email()),
                clean(data.indirizzoCasa()),
                fileAllegati,
                clean(data.eta()),
                clean(data.professione()),
                clean(data.diagnosi()),
                clean(data.forzaMuscolare()),
                clean(data.naturaProblema()),
                clean(data.esamiStrumentaliInVisione()),
                clean(data.core()),
                clean(data.anamnesiRemota()),
                clean(data.anamnesiProssima()),
                clean(data.terapieSvolte()),
                clean(data.testSpecificiSomministrati()),
                clean(data.misurazionePrestazione()),
                clean(data.valutazioneEquilibrio()),
                clean(data.valutazioneRomAaromProm()),
                clean(data.valutazioneElasticitaMuscolare()),
                clean(data.valutazionePosturale()),
                clean(data.note())
        );
    }

    private ServiceResult<Paziente> validatePatientFields(PatientClinicalData data) {
        if (!ValidationUtil.isValidName(data.nome())) {
            return ServiceResult.failure("Il nome deve contenere almeno 2 caratteri.");
        }

        if (!ValidationUtil.isValidName(data.cognome())) {
            return ServiceResult.failure("Il cognome deve contenere almeno 2 caratteri.");
        }

        if (!ValidationUtil.isValidPhone(data.telefono())) {
            return ServiceResult.failure("Il numero di telefono contiene caratteri non validi.");
        }

        if (data.email() != null && !data.email().isBlank() && !ValidationUtil.isValidEmail(data.email())) {
            return ServiceResult.failure("Inserisci un indirizzo email valido.");
        }

        if (data.indirizzoCasa() == null || data.indirizzoCasa().trim().length() < 3) {
            return ServiceResult.failure("Inserisci un indirizzo di casa valido.");
        }

        if (data.eta() != null && !data.eta().isBlank()) {
            try {
                int age = Integer.parseInt(data.eta().trim());

                if (age < 0 || age > 130) {
                    return ServiceResult.failure("Inserisci un'età valida.");
                }

            } catch (NumberFormatException e) {
                return ServiceResult.failure("L'età deve essere un numero.");
            }
        }

        return ServiceResult.success("Validazione completata.", null);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public record PatientClinicalData(
            String nome,
            String cognome,
            String telefono,
            String email,
            String indirizzoCasa,
            String eta,
            String professione,
            String diagnosi,
            String forzaMuscolare,
            String naturaProblema,
            String esamiStrumentaliInVisione,
            String core,
            String anamnesiRemota,
            String anamnesiProssima,
            String terapieSvolte,
            String testSpecificiSomministrati,
            String misurazionePrestazione,
            String valutazioneEquilibrio,
            String valutazioneRomAaromProm,
            String valutazioneElasticitaMuscolare,
            String valutazionePosturale,
            String note
    ) {
    }

    public static final class ServiceResult<T> {

        private final boolean success;
        private final String message;
        private final T value;

        private ServiceResult(boolean success, String message, T value) {
            this.success = success;
            this.message = message;
            this.value = value;
        }

        public static <T> ServiceResult<T> success(String message, T value) {
            return new ServiceResult<>(true, message, value);
        }

        public static <T> ServiceResult<T> failure(String message) {
            return new ServiceResult<>(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public T getValue() {
            return value;
        }
    }
}
