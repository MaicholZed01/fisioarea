package com.fisioarea.service;

import com.fisioarea.model.Appuntamento;
import com.fisioarea.repository.AppuntamentoRepository;

import java.time.LocalDate;
import java.util.List;

public class AppuntamentoService {

    private final AppuntamentoRepository appuntamentoRepository = new AppuntamentoRepository();

    public List<Appuntamento> getAppuntamenti() {
        return appuntamentoRepository.findAll();
    }

    public List<Appuntamento> getAppuntamentiDelGiorno(LocalDate date) {
        return appuntamentoRepository.findByDate(date);
    }

    public Appuntamento salvaAppuntamento(Appuntamento appuntamento) {
        return appuntamentoRepository.save(appuntamento);
    }

    public Appuntamento aggiornaAppuntamento(Appuntamento appuntamento) {
        return appuntamentoRepository.update(appuntamento);
    }

    public void eliminaAppuntamento(int id) {
        appuntamentoRepository.deleteById(id);
    }
}
