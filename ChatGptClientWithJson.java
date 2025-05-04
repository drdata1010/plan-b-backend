import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatGptClientWithJson {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OPENAI_API_KEY") != null ? System.getenv("OPENAI_API_KEY") : "";
    private static final String MODEL = "gpt-4o";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("ChatGPT Terminal Client");
        System.out.println("Type 'exit' to quit");
        System.out.println("---------------------------");

        while (true) {
            System.out.print("\nYou: ");
            String userInput = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userInput)) {
                System.out.println("Goodbye!");
                break;
            }

            try {
                String response = callChatGptApi(userInput);
                System.out.println("\nChatGPT: " + response);
            } catch (IOException e) {
                System.out.println("Error calling ChatGPT API: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static String callChatGptApi(String userMessage) throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        String jsonInputString = String.format(
            "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"temperature\": 0.7}",
            MODEL, userMessage.replace("\"", "\\\""));

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        // Extract the content using regex
        String jsonResponse = response.toString();
        Pattern pattern = Pattern.compile("\"content\":\"(.*?)\"(?:,|\\})");
        Matcher matcher = pattern.matcher(jsonResponse);

        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        } else {
            return "Error parsing response: " + jsonResponse;
        }
    }
}
