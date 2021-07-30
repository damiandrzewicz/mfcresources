package de.atos.solumversion.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MfcProjectDescriptor {

    private String visualStudioVersion;

    private Map<String, String> projects = new HashMap<>();
}
