package printer;

public class SystemOutPrinter implements IPrinter {

    @Override
    public void print(String output) {
        System.out.println(output);
    }
}
