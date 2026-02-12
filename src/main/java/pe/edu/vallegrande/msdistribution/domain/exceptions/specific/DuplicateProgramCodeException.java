package pe.edu.vallegrande.msdistribution.domain.exceptions.specific;

import pe.edu.vallegrande.msdistribution.domain.exceptions.base.ConflictException;

public class DuplicateProgramCodeException extends ConflictException {
    public DuplicateProgramCodeException(String programCode) {
        super("Distribution Program with code '" + programCode + "' already exists", "DUPLICATE_PROGRAM_CODE");
    }
}
