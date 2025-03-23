package feeder.prompt;

import lombok.Getter;
import printer.SystemOutPrinter;

public abstract class ClientCommand implements ICommand {
    @Getter
    public String commandName;

    protected SystemOutPrinter systemOutPrinter;

    public ClientCommand(SystemOutPrinter systemOutPrinter) {
        this.systemOutPrinter = systemOutPrinter;
    }

    public void execute(String[] command, String commandText) {
        boolean validate = validate(command);
        if (!validate) {
            return;
        }

        action(command, commandText);
    }

    abstract boolean validate(String[] command);
}
