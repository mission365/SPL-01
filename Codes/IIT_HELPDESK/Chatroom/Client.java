import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
public class Client {
    RSAKeyGen keyGenerator = new RSAKeyGen(1024);
    BigInteger publicKey = keyGenerator.getPublicKey();
    BigInteger privateKey = keyGenerator.getPrivateKey();
    BigInteger modulus = keyGenerator.getModulus();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    private String filename1 = "encryptedMessages.txt";

    public Client(Socket socket, String  username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                if (messageToSend.startsWith("@file")) {
                    int spaceIndex = messageToSend.indexOf(" ");
                    String recipientUsername = messageToSend.substring(spaceIndex + 1);
                    System.out.print("Enter file path: ");
                    String filePath = scanner.nextLine();
                    sendFile(recipientUsername, filePath);
                }
                else if (messageToSend.startsWith("@")) {
                    // Private message
                    BigInteger message = new BigInteger(messageToSend.getBytes());
                    BigInteger encryptedMessage = EncryptionDecryption.encrypt(message, publicKey, modulus);
                    BigInteger decryptedMessage = EncryptionDecryption.decrypt(encryptedMessage, privateKey, modulus);
                    bufferedWriter.write(new String(decryptedMessage.toByteArray())); //send message to the server
                    try {
                        FileWriter writer = new FileWriter(filename1, true);
                        writer.write("encryptedMessage:"+encryptedMessage.toString());
                        writer.write(System.lineSeparator());
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    // Normal message
                    BigInteger message = new BigInteger(messageToSend.getBytes());
                    BigInteger encryptedMessage = EncryptionDecryption.encrypt(message, publicKey, modulus);
                    BigInteger decryptedMessage = EncryptionDecryption.decrypt(encryptedMessage, privateKey, modulus);
                    bufferedWriter.write(username + ": " + new String(decryptedMessage.toByteArray()));

                    try {
                        FileWriter writer = new FileWriter(filename1, true);
                        writer.write("encryptedMessage:"+encryptedMessage.toString());
                        writer.write(System.lineSeparator());
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    public void listenMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromgrpchat;

                while(socket.isConnected()) {
                    try {
                        msgFromgrpchat = bufferedReader.readLine();
                        System.out.println(msgFromgrpchat);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }
    public void sendFile(String recipientUsername, String filePath) {
        try {
            Path path = Paths.get(filePath);
            byte[] fileBytes = Files.readAllBytes(path);
            String fileName = path.getFileName().toString();


            bufferedWriter.write("@file " + recipientUsername + " " + fileName);
            bufferedWriter.newLine();
            bufferedWriter.flush();


            bufferedWriter.write(new String(fileBytes));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
            if(bufferedWriter != null) {
                bufferedWriter.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        Authentication auth = new Authentication();

        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Registration");
        System.out.println("2. Login\n");

        System.out.print("Enter Your Choice: ");
        int choice = scanner.nextInt();
        String username, password,tmp;

        boolean token = false;

        switch(choice) {
            case 1: {
                System.out.print("Enter Your Username: ");
                username = scanner.next();

                System.out.print("Enter Your Password: ");
                password = scanner.next();

                if (password.indexOf(' ') == -1) {
                    token = auth.handleRegistration(username, password);
                } else {
                    System.out.println("Password Should Not Contain Any Space");
                    token = false;
                }

                break;
            }
            case 2: {
                System.out.print("Enter Your Username: ");
                username = scanner.next();

                System.out.print("Enter Your Password: ");
                password = scanner.next();

                if (password.indexOf(' ') == -1) {
                    token = auth.handleLogin(username, password);
                } else {
                    System.out.println("Password Should Not Contain Any Space");
                    token = false;
                }

                break;
            }
            default: {
                username = "";
                System.out.println("Invalid Choice");
            }
        }

        if (token) {
            Socket socket = new Socket("192.168.132.28", 1234);
            Client client = new Client(socket, username);
            client.listenMessage();
            client.sendMessage();
        }
    }


}

