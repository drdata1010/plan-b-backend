package com.planb.supportticket.cli;

import com.planb.supportticket.service.ChatGptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * Command-line interface for ChatGPT.
 * This will only run if the 'chatgpt.cli.enabled' property is set to true.
 */
@Component
@ConditionalOnProperty(name = "chatgpt.cli.enabled", havingValue = "true")
public class ChatGptCli implements CommandLineRunner {

    private final ChatGptService chatGptService;

    @Autowired
    public ChatGptCli(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }

    @Override
    public void run(String... args) {
        System.out.println("ChatGPT Terminal Client");
        System.out.println("Type 'exit' to quit");
        System.out.println("---------------------------");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nYou: ");
            String userInput = scanner.nextLine();
            
            if ("exit".equalsIgnoreCase(userInput)) {
                System.out.println("Goodbye!");
                break;
            }
            
            try {
                String response = chatGptService.sendMessage(userInput);
                System.out.println("\nChatGPT: " + response);
            } catch (Exception e) {
                System.out.println("Error calling ChatGPT API: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
}
