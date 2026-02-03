import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class MusicClient {

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 9876;

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            socket.setSoTimeout(5000);
            InetAddress address = InetAddress.getByName(hostname);

            // login: invio username e password
            System.out.print("Inserisci il tuo username: ");
            String user = scanner.nextLine();
            System.out.print("Inserisci la tua password: ");
            String pass = scanner.nextLine();

            String login = user + ":" + pass;
            byte[] sendBuffer = login.getBytes();

            DatagramPacket packetToSend =
                    new DatagramPacket(sendBuffer, sendBuffer.length, address, port);
            socket.send(packetToSend);

            // ricezione risposta login + lista canzoni
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket packetReceived =
                    new DatagramPacket(receiveBuffer, receiveBuffer.length);

            socket.receive(packetReceived);
            String risposta = new String(packetReceived.getData(), 0, packetReceived.getLength());
            System.out.println("server: " + risposta);

            // visualizzo la lista canzoni estratta dal messaggio ricevuto (opzionale)
            if (risposta.contains("Brani disponibili:")) {
                String lista = risposta.substring(risposta.indexOf("Brani disponibili:") + 19);
            }

            // richiesta titolo canzone
            System.out.print("Inserisci il titolo della canzone: ");
            String titolo = scanner.nextLine();
            sendBuffer = titolo.getBytes();

            packetToSend =
                    new DatagramPacket(sendBuffer, sendBuffer.length, address, port);
            socket.send(packetToSend);

            // ricezione risposta server sul file
            socket.receive(packetReceived);
            String info =
                    new String(packetReceived.getData(), 0, packetReceived.getLength());

            if (info.startsWith("file")) {

                // ricezione file
                DatagramPacket filePacket =
                        new DatagramPacket(new byte[1024], 1024);
                socket.receive(filePacket);

                // salvataggio file nella cartella downloads
                String userHome = System.getProperty("user.home");
                File downloadDir = new File(userHome, "Downloads");
                if (!downloadDir.exists()) downloadDir.mkdirs();

                File file = new File(downloadDir, titolo + ".mp3");

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(filePacket.getData(), 0, filePacket.getLength());
                fos.close();

                System.out.println("La canzone è stata scaricata in:");
                System.out.println(file.getAbsolutePath());

            } else {
                System.out.println("errore server: " + info);
            }

        } catch (IOException e) {
            System.err.println("errore o timeout: " + e.getMessage());
        }
    }
}
