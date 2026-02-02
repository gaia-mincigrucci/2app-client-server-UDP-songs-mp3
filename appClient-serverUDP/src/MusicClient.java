import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MusicClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 9876;

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress address = InetAddress.getByName(host);

            // --- FASE 1: LOGIN SEPARATO ---
            System.out.println("=== ACCESSO AL SERVIZIO MUSICALE ===");
            System.out.print("Inserisci il tuo username: ");
            String user = scanner.nextLine();
            System.out.print("Inserisci la tua password: ");
            String pass = scanner.nextLine();

            String credentials = user + ":" + pass;
            byte[] credBytes = credentials.getBytes();
            socket.send(new DatagramPacket(credBytes, credBytes.length, address, port));

            byte[] buffer = new byte[1024];
            DatagramPacket resp = new DatagramPacket(buffer, buffer.length);
            socket.receive(resp);
            System.out.println("\n[SERVER]: " + new String(resp.getData(), 0, resp.getLength()).trim());

            // --- FASE 2: RICHIESTA BRANO ---
            System.out.print("\nInserisci il titolo della canzone che vuoi scaricare: ");
            String titolo = scanner.nextLine();
            byte[] titoloBytes = titolo.getBytes();
            socket.send(new DatagramPacket(titoloBytes, titoloBytes.length, address, port));

            // --- FASE 3: RICEZIONE E SALVATAGGIO ---
            socket.receive(resp);
            String info = new String(resp.getData(), 0, resp.getLength()).trim();

            // CORREZIONE QUI: mancavano le virgolette e la parentesi tonda
            if (info.startsWith("FILE_OK")) {
                System.out.println("Ricezione in corso... " + info);

                // Riceve i byte effettivi del file
                DatagramPacket filePacket = new DatagramPacket(new byte[1024], 1024);
                socket.receive(filePacket);

                // Salviamo il file nella cartella del progetto
                FileOutputStream fos = new FileOutputStream("canzone_scaricata.mp3");
                fos.write(filePacket.getData(), 0, filePacket.getLength());
                fos.close();
                System.out.println("SUCCESS: Brano salvato come 'canzone_scaricata.mp3'");
            } else {
                System.out.println("[SERVER ERROR]: " + info);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}