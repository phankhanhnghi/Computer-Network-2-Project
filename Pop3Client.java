import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Pop3Client {
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 110; // POP3 port is 110

        Scanner userInput = new Scanner(System.in);
        int loginAttempts = 0;

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("Server: " + response);
                if (response.startsWith("+OK")) {
                    break;
                }
            }

            while (loginAttempts < MAX_LOGIN_ATTEMPTS) {
                System.out.print("Enter email address: ");
                String email = userInput.nextLine();
                System.out.print("Enter password: ");
                String password = userInput.nextLine();

                out.println("USER " + email);
                response = in.readLine();
                System.out.println("Server: " + response);

                out.println("PASS " + password);
                response = in.readLine();
                System.out.println("Server: " + response);

                if (response.startsWith("+OK")) {
                    retrieveEmails(userInput, in, out);
                    break;
                } else {
                    loginAttempts++;
                    System.out.println("Login failed. You have " + (MAX_LOGIN_ATTEMPTS - loginAttempts) + " attempts left.");
                }
            }

            if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                System.out.println("Maximum login attempts exceeded. Exiting...");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            userInput.close();
        }
    }

    private static void retrieveEmails(Scanner userInput, BufferedReader in, PrintWriter out) throws IOException {
        String command;
        while (true) {
            System.out.print("Enter command (LIST/RETR/QUIT): ");
            command = userInput.nextLine();
            out.println(command);

            String response = in.readLine();
            if (response == null) {
                System.out.println("Connection closed by server.");
                break;
            }
            System.out.println("Server: " + response);

            if (command.toUpperCase().startsWith("LIST")) {
                while (!(response = in.readLine()).equals(".")) {
                    System.out.println("Server: " + response);
                }
            } else if (command.toUpperCase().startsWith("RETR")) {
                while (!(response = in.readLine()).equals(".")) {
                    System.out.println("Server: " + response);
                }
            } else if (command.toUpperCase().startsWith("QUIT")) {
                break;
            }
        }
    }
}
