package feeder.prompt;

public interface ICommand {
    void action(String[] command, String commandText);

    void execute(String[] command, String commandText);
}