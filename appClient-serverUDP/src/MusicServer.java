import java.io.*;
import java.net.*;

public class MusicServer {
    public static void main(String[] args) {
        int port = 9876;

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("SERVER MUSICA in ascolto sulla porta " + port);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length);

                // ===== LOGIN =====
                socket.receive(packet);
                InetAddress clientAddr = packet.getAddress();
                int clientPort = packet.getPort();

                String loginOK = "LOGIN OK";
                socket.send(new DatagramPacket(
                        loginOK.getBytes(),
                        loginOK.length(),
                        clientAddr,
                        clientPort
                ));

                // ===== RICHIESTA CANZONE =====
                socket.receive(packet);
                String titolo =
                        new String(packet.getData(), 0, packet.getLength()).trim();

                Canzone canzone = new Canzone(titolo);
                File file = new File(canzone.getNomeFileCompleto());

                if (file.exists()) {
                    String ok = "FILE:" + file.getName();
                    socket.send(new DatagramPacket(
                            ok.getBytes(),
                            ok.length(),
                            clientAddr,
                            clientPort
                    ));

                    FileInputStream fis = new FileInputStream(file);
                    byte[] fileBytes = new byte[1024];
                    int letti = fis.read(fileBytes);

                    socket.send(new DatagramPacket(
                            fileBytes,
                            letti,
                            clientAddr,
                            clientPort
                    ));
                    fis.close();
                } else {
                    String err = "ERRORE: BRANO NON TROVATO";
                    socket.send(new DatagramPacket(
                            err.getBytes(),
                            err.length(),
                            clientAddr,
                            clientPort
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
