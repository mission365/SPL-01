import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket s;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket s) {
        try {
            this.s = s;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("Server: " + clientUsername + " has joined the chatroom.");
        } catch (IOException e) {
            closeEverything(s, bufferedReader, bufferedWriter);
        }
    }

    public ClientHandler(String ip,int port) throws IOException {
        s = new Socket(ip, port);
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public void run() {
        String messageFromClient;

        while (s.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if(messageFromClient.startsWith("@file")) {
                    handleFileTransfer(messageFromClient);
                }
                else {
                    broadcastMessage(messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(s, bufferedReader, bufferedWriter);
                break;
            }
        }
    }
    public void broadcastMessage(String messageToSend) {
        if (messageToSend.startsWith("@")) {
            // Private message
            int spaceIndex = messageToSend.indexOf(" ");
            if (spaceIndex != -1) {
                String recipientUsername = messageToSend.substring(1, spaceIndex);
                String privateMessage = messageToSend.substring(spaceIndex + 1);
                sendPrivateMessage(recipientUsername, privateMessage);
            }
        } else {
            // Broadcast message
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    if (!clientHandler.clientUsername.equals(clientUsername)) {
                        clientHandler.bufferedWriter.write(messageToSend);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(s, bufferedReader, bufferedWriter);
                }
            }
        }
    }

    private void sendPrivateMessage(String recipientUsername, String privateMessage) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUsername.equals(recipientUsername)) {
                try {
                    clientHandler.bufferedWriter.write("(Private) " + clientUsername + ": " + privateMessage);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } catch (IOException e) {
                    closeEverything(s, bufferedReader, bufferedWriter);
                }
                break;
            }
        }
    }
    private void handleFileTransfer(String fileMessage) {
        String[] parts = fileMessage.split(" ");
        String recipientUsername = parts[1];
        String fileName = parts[2];

        StringBuilder fileData = new StringBuilder();
        try {
            String line;
            while (!(line = bufferedReader.readLine()).isEmpty()) {
                fileData.append(line);
                fileData.append(System.lineSeparator());
            }

            for (ClientHandler clientHandler : clientHandlers) {
                if (clientHandler.clientUsername.equals(recipientUsername)) {
                    clientHandler.bufferedWriter.write("(File) " + clientUsername + " sent a file: " + fileName);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.write(fileData.toString());
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("Server: "+ clientUsername + " has left the chatroom.");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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
}
