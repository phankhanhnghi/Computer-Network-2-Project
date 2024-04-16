import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pop3Server {
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static Map<String, String> userAccounts = new HashMap<>();
    private static Map<String, List<String>> userEmails = new HashMap<>();

    public static void main(String[] args) {
        // Initialize user accounts and associated emails
        userAccounts.put("user1@example.com", "password1");
        userAccounts.put("user2@example.com", "password2");
        // Add emails for each user
        List<String> user1Emails = new ArrayList<>();
        user1Emails.add("From: sender1@example.com\nTo: user1@example.com\nSubject: Email 1\n\nBody of email 1");
        user1Emails.add("From: sender2@example.com\nTo: user1@example.com\nSubject: Email 2\n\nBody of email 2");
        userEmails.put("user1@example.com", user1Emails);
        // Add emails for other users similarly...

        try {
            ServerSocket serverSocket = new ServerSocket(110); // POP3 port is 110
            System.out.println("POP3 Server is running...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection established with " + clientSocket.getInetAddress());
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
    
        out.println("+OK POP3 server ready");
    
        String inputLine;
        int loginAttempts = 0;
        boolean isAuthenticated = false;
        String currentUser = null;
    
        while ((inputLine = in.readLine()) != null) {
            if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                out.println("-ERR Maximum login attempts exceeded");
                break;
            }
            if (!isAuthenticated) {
                // Handle authentication
                if (inputLine.toUpperCase().startsWith("USER")) {
                    String[] tokens = inputLine.split(" ");
                    if (tokens.length == 2) {
                        String email = tokens[1].trim();
                        if (userAccounts.containsKey(email)) {
                            out.println("+OK User accepted");
                            currentUser = email;
                        } else {
                            out.println("-ERR Invalid user");
                            loginAttempts++;
                        }
                    } else {
                        out.println("-ERR Invalid command");
                    }
                } else if (inputLine.toUpperCase().startsWith("PASS")) {
                    String[] tokens = inputLine.split(" ");
                    if (tokens.length == 2) {
                        String password = tokens[1].trim();
                        if (currentUser != null && userAccounts.containsKey(currentUser) &&
                                userAccounts.get(currentUser).equals(password)) {
                            out.println("+OK Password accepted");
                            isAuthenticated = true;
                        } else {
                            out.println("-ERR Invalid password");
                            loginAttempts++;
                        }
                    } else {
                        out.println("-ERR Invalid command");
                    }
                } else {
                    out.println("-ERR Authentication required");
                }
            } else {
                // Handle commands after authentication
                if (inputLine.toUpperCase().startsWith("LIST")) {
                    if (userEmails.containsKey(currentUser)) {
                        List<String> emails = userEmails.get(currentUser);
                        out.println("+OK " + emails.size() + " messages");
                        for (int i = 0; i < emails.size(); i++) {
                            out.println((i + 1) + " " + emails.get(i).length());
                        }
                        out.println(".");
                    } else {
                        out.println("-ERR No emails found");
                    }
                } else if (inputLine.toUpperCase().startsWith("RETR")) {
                    int emailNumber = Integer.parseInt(inputLine.split(" ")[1]) - 1;
                    if (userEmails.containsKey(currentUser)) {
                        List<String> emails = userEmails.get(currentUser);
                        if (emailNumber >= 0 && emailNumber < emails.size()) {
                            out.println("+OK " + emails.get(emailNumber).length() + " octets");
                            out.println(emails.get(emailNumber));
                            out.println(".");
                        } else {
                            out.println("-ERR Message not found");
                        }
                    } else {
                        out.println("-ERR No emails found");
                    }
                } else if (inputLine.toUpperCase().startsWith("QUIT")) {
                    out.println("+OK POP3 server signing off");
                    break;
                } else {
                    out.println("-ERR Unknown command");
                }
            }
        }
    
        out.close();
        in.close();
        clientSocket.close();
    }
}
