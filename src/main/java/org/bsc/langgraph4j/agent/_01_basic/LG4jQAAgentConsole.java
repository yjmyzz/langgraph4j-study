package org.bsc.langgraph4j.agent._01_basic;

import org.bsc.langgraph4j.agent._01_basic.langgraph.QAAssistant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.Scanner;

@Component
public class LG4jQAAgentConsole implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        Console console = System.console();
        Scanner scanner = null;

        // 如果 console 为 null（在 IDE 或非交互式环境中），使用 Scanner
        if (console == null) {
            scanner = new Scanner(System.in);
        }

        var assistant = new QAAssistant();

        var question = readLine(console, scanner, "\nQuestion: ");

        var output = assistant.startConversation(question);

        do {
            printf(console, scanner, "\nAgent:\n%s", output.state().messages());
            var message = readLine(console, scanner, "\nAnswer: ");
            output = assistant.provideFeedback(message);

        } while (!output.isEND());

        printf(console, scanner, "\nResult: \n%s", output.state().messages());

        if (scanner != null) {
            scanner.close();
        }
    }

    private String readLine(Console console, Scanner scanner, String prompt) {
        if (console != null) {
            return console.readLine(prompt);
        } else {
            System.out.print(prompt);
            return scanner.nextLine();
        }
    }

    private void printf(Console console, Scanner scanner, String format, Object... args) {
        if (console != null) {
            console.printf(format, args);
        } else {
            System.out.printf(format, args);
        }
    }
}
