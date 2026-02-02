import java.io.*;
import java.net.*;

public class MusicServer {
    public static void main(String[] args) {
        int port = 9876;
        String[] catalogo = {"canzone.mp3", "test.mp3"};

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("SERVER MUSICA: In ascolto sulla porta " + port);

            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                // 1. Attesa Login
                socket.receive(receivePacket);
                String loginData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress clientAddr = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // Il server si aspetta "user:pass"
                if (loginData.contains(":")) {
                    String response = "LOGIN EFFETTUATO. Brani disponibili: " + String.join(", ", catalogo);
                    socket.send(new DatagramPacket(response.getBytes(), response.length(), clientAddr, clientPort));
                    System.out.println("Utente autenticato: " + clientAddr);
                }

                // 2. Attesa Richiesta Canzone
                socket.receive(receivePacket);
                String titoloRichiesto = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();

                // Uso della tua classe Canzone.java
                Canzone canzone = new Canzone(titoloRichiesto);
                File file = new File(canzone.getNomeFileCompleto());

                if (file.exists()) {
                    // Conferma file trovato
                    String msg = "FILE:" + file.getName() + " (" + file.length() + " bytes)";
                    socket.send(new DatagramPacket(msg.getBytes(), msg.length(), clientAddr, clientPort));

                    // Invio dei byte (Anteprima)
                    FileInputStream fis = new FileInputStream(file);
                    byte[] fileContent = new byte[1024];
                    int letti = fis.read(fileContent);
                    socket.send(new DatagramPacket(fileContent, letti, clientAddr, clientPort));
                    fis.close();
                } else {
                    String errore = "ERRORE: BRANO NON TROVATO.";
                    socket.send(new DatagramPacket(errore.getBytes(), errore.length(), clientAddr, clientPort));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}