package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcSubversionProject;
import de.atos.solumversion.repositories.LocalSubversionProjectsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavedSubversionProjectsService {

    private LocalSubversionProjectsRepository localSubversionProjectsRepository;

    public SavedSubversionProjectsService(LocalSubversionProjectsRepository localSubversionProjectsRepository) {
        this.localSubversionProjectsRepository = localSubversionProjectsRepository;
    }

    public List<MfcSubversionProject> findAll(){

    }
}
