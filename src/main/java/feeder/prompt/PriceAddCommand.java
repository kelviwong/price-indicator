package feeder.prompt;

import data.Event;
import data.Price;
import feeder.PriceFeeder;
import printer.SystemOutPrinter;

public class PriceAddCommand extends ClientCommand {

    PriceFeeder<String> cmdPriceFeeder;

    public PriceAddCommand(PriceFeeder<String> cmdPriceFeeder, SystemOutPrinter systemOutPrinter) {
        super(systemOutPrinter);
        this.commandName = "priceAdd";
        this.cmdPriceFeeder = cmdPriceFeeder;
    }

    @Override
    boolean validate(String[] command) {
        if (command.length != 6) {
            systemOutPrinter.print("Invalid number of arguments!, e.g. addprice 9:30 AM AUD/USD 0.6905 106,198");
            return false;
        }

        return true;
    }
    @Override
    public void action(String[] command, String commandText) {
        systemOutPrinter.print("pushing price data: " + commandText);
        cmdPriceFeeder.pushData(commandText.trim());
    }
}
