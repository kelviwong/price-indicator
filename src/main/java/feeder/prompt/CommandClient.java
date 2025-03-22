package feeder.prompt;

import common.NamedThreadFactory;
import enums.CommandType;
import feeder.PriceFeeder;
import printer.SystemOutPrinter;
import service.IService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class CommandClient implements IService {
    private final Map<CommandType, ICommand> commandMap;
    private final SystemOutPrinter systemOutPrinter;
    private static final String delimiter = "-";
    private ExecutorService executorService;

    public CommandClient(PriceFeeder<String> cmdPriceFeeder, SystemOutPrinter systemOutPrinter) {
        this.commandMap = new HashMap<>();
        this.commandMap.put(CommandType.PRICE_ADD, new PriceAddCommand(cmdPriceFeeder, systemOutPrinter));
        this.commandMap.put(CommandType.Exit, new ExitCommand(systemOutPrinter));
        this.systemOutPrinter = systemOutPrinter;
    }

    public void start() {
        try {
            executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("CommandClient"));
            executorService.submit(this::LoadPrompt);
        } catch (Exception e) {
            systemOutPrinter.print(e.getMessage());
        }
    }

    public void stop() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                systemOutPrinter.print("Timeout: force shutdown");
                executorService.shutdownNow();
            } else {
                systemOutPrinter.print("task is done");
            }
        } catch (InterruptedException e) {
            systemOutPrinter.print("InterruptedException: force shutdown");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void LoadPrompt() {
        SystemOutPrinter systemOutPrinter = new SystemOutPrinter();
        String instruction = "Enter your price in addprice-9:30 AM;AUD/USD;0.6905;106,198, exit to quit";
        systemOutPrinter.print(instruction);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            try {
                String commandText = scanner.nextLine();
                if (commandText.isEmpty()) {
                    continue;
                }

                String[] commandSplit = commandText.split(delimiter);
                if (commandSplit.length < 1) {
                    systemOutPrinter.print("Incorrect format: " + commandText);
                    continue;
                }

                String action = commandSplit[0].trim();
                Optional<CommandType> commandType = CommandType.valueFrom(action);
                if (!commandType.isPresent()) {
                    systemOutPrinter.print("Wrong command type: " + commandText);
                    continue;
                }

                ICommand command = commandMap.get(commandType.get());
                systemOutPrinter.print("Price input: " + commandText);
                command.execute(commandSplit);

                systemOutPrinter.print("");
                systemOutPrinter.print(instruction);
            } catch (Exception e) {
                systemOutPrinter.print(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
