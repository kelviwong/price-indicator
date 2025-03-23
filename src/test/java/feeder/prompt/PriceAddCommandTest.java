package feeder.prompt;

import feeder.CmdPriceFeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import printer.SystemOutPrinter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceAddCommandTest {

    private PriceAddCommand priceAddCommand;
    @Mock
    CmdPriceFeeder cmdPriceFeeder;

    @BeforeEach
    public void setup() {
        SystemOutPrinter systemOutPrinter = new SystemOutPrinter();
        priceAddCommand = new PriceAddCommand(cmdPriceFeeder, systemOutPrinter);
    }

    @Test
    public void testCommandValidateFail() {
        String command = "addprice 9:30 AM 100 10";
        String[] commands = command.split(" ");
        boolean validate = priceAddCommand.validate(commands);
        assertFalse(validate);

        command = "addprice 9:30 AM EUR/USD 100 10";
        commands = command.split(" ");
        validate = priceAddCommand.validate(commands);
        assertTrue(validate);
    }

    @Test
    public void testCommandAction() {
        String command = "addprice 9:30 AM 100 10";
        String[] commands = command.split(" ");
        String commandText = command.substring("addprice".length());
        priceAddCommand.action(commands, commandText.trim());
        verify(cmdPriceFeeder, times(1)).pushData(any());
    }

}