package feeder.prompt;

public interface ICommand {
    void action(String[] command);
    void execute(String[] command);
}