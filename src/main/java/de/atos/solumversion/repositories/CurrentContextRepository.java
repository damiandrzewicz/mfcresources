package de.atos.solumversion.repositories;

import de.atos.solumversion.domain.MfcSubversionProject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
@Setter
@Getter
public class CurrentContextRepository {

    private MfcSubversionProject project;
}
