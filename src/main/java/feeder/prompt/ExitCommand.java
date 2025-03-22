package feeder.prompt;

import printer.SystemOutPrinter;

public class ExitCommand extends ClientCommand {

    public ExitCommand(SystemOutPrinter systemOutPrinter) {
        super(systemOutPrinter);
        this.commandName = "exit";
    }

    @Override
    boolean validate(String[] command) {
        return true;
    }

    @Override
    public void action(String[] command) {
        System.exit(0);
    }
}
