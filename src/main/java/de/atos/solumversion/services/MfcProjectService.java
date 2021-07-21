package de.atos.solumversion.services;

import de.atos.solumversion.dto.ProjectDTO;
import de.atos.solumversion.dto.ResourceDTO;

import java.util.List;

public interface MfcProjectService {

    ProjectDTO fetchProject(ProjectDTO projectDTO) throws MfcProjectServiceException;

    ProjectDTO updateWholeWorkingCopyProject(ProjectDTO projectDTO) throws MfcProjectServiceException;

    List<ProjectDTO> getWorkingCopyProjects() throws MfcProjectServiceException;

    List<ResourceDTO> getProjectResources(ProjectDTO projectDTO) throws MfcProjectServiceException;

//    void remove(SvnItemDTO svnItemDTO);
//
//    SvnItemDTO update(SvnItemDTO svnItemDTO);
//
//    CommitDTO commit(CommitDTO commitDTO);

}
