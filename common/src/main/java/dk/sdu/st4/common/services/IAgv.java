package dk.sdu.st4.common.services;

import dk.sdu.st4.common.data.enums.AgvProgram;
import dk.sdu.st4.common.data.AgvStatus;

public interface IAgv {
    void loadProgram(AgvProgram program) throws Exception;
    void executeProgram() throws Exception;
    AgvStatus getStatus() throws Exception;
}
