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

            // ===== LOGIN =====
            System.out.println("=== ACCESSO AL SERVIZIO MUSICALE ===");
            System.out.print("Username: ");
            String user = scanner.nextLine();
            System.out.print("Password: ");
            String pass = scanner.nextLine();

            String credentials = user + ":" + pass;
            socket.send(new DatagramPacket(
                    credentials.getBytes(),
                    credentials.length(),
                    address,
                    port
            ));

            byte[] buffer = new byte[1024];
            DatagramPacket resp = new DatagramPacket(buffer, buffer.length);
            socket.receive(resp);

            System.out.println("[SERVER] " +
                    new String(resp.getData(), 0, resp.getLength()));

            // ===== RICHIESTA CANZONE =====
            System.out.print("\nInserisci il titolo della canzone: ");
            String titolo = scanner.nextLine();

            socket.send(new DatagramPacket(
                    titolo.getBytes(),
                    titolo.length(),
                    address,
                    port
            ));

            // ===== RISPOSTA SERVER =====
            socket.receive(resp);
            String info = new String(resp.getData(), 0, resp.getLength()).trim();

            if (info.startsWith("FILE:")) {
                System.out.println("Download in corso...");

                // Ricezione file
                DatagramPacket filePacket =
                        new DatagramPacket(new byte[1024], 1024);
                socket.receive(filePacket);

                // ===== SALVATAGGIO SU PC (CARTELLA DOWNLOAD) =====
                String userHome = System.getProperty("user.home");
                File downloadDir = new File(userHome, "Downloads");

                if (!downloadDir.exists()) {
                    downloadDir.mkdirs();
                }

                File file = new File(downloadDir, titolo + ".mp3");

                if (file.exists()) {
                    System.out.println("ERRORE: Il file esiste già nei Download.");
                    return;
                }

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(filePacket.getData(), 0, filePacket.getLength());
                fos.close();

                System.out.println("SUCCESSO: File salvato in");
                System.out.println(file.getAbsolutePath());
            } else {
                System.out.println("[ERRORE SERVER] " + info);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
