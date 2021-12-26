package fr.insalyon.p2i2.javaarduino.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Console {

    public final BufferedReader input;
    public final PrintStream output = System.out;
    public final PrintStream log = System.err;

    public Console() {

        this.input = new BufferedReader(new InputStreamReader(System.in));
    }

    public String readLine(String header) throws IOException {

        output.print(header);
        output.flush();
        return input.readLine();
    }

    public void log(String line) {
        log.println(line);
    }

    public void log(Throwable th) {
        th.printStackTrace(log);
    }

    public void println(String line) {
        output.println(line);
    }

}
