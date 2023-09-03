import java.io.*;
import java.util.HashMap;

class Authentication {
    String userDataFile = "userData.txt";

    HashMap<String, String> extractAllUserList() {
        HashMap<String, String> allUserList = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(userDataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] components = line.split(" ");
                allUserList.put(components[0], components[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allUserList;
    }

    boolean handleRegistration(String username, String password) {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            HashMap<String, String> allUserList = extractAllUserList();
            if (allUserList.containsKey(username)) {
                System.out.println("User is Already Exist");
                return false;
            } else {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(userDataFile, true))) {
                    writer.write(username + " " + password + System.lineSeparator()); // Adding a newline
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Registraton Successfull");
                return true;
            }
        } else {
            System.out.println("Invalid User");
            return false;
        }
    }
        boolean handleLogin (String username, String password){
            HashMap<String, String> allUserList = extractAllUserList();
            if (allUserList.containsKey(username)) {
                String storedPassword = allUserList.get(username);
                if (storedPassword.equals(password)) {
                    System.out.println("Login Successfull");
                    return true;
                } else {
                    System.out.println("Password is not Matched");
                    return false;
                }
            } else {
                System.out.println("User is Not Registered");
                return false;
            }
        }
}

