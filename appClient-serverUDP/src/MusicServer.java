import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MusicServer {

    public static void main(String[] args) {
        int port = 9876;
        // catalogo delle canzoni disponibili (modifica qui per aggiungere)
        String[] catalogo = {"canzone.mp3", "test.mp3"};

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Music server UDP avviato sulla porta " + port);

            byte[] receiveBuffer = new byte[1024];

            while (true) {

                // ricezione login
                DatagramPacket receivePacket =
                        new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                System.out.println("richiesta login da " +
                        clientAddress + ":" + clientPort);

                String login =
                        new String(receivePacket.getData(), 0, receivePacket.getLength());

                // invia conferma login + lista canzoni disponibili
                String lista = String.join(", ", catalogo);
                String loginOK = "login effettuato\nBrani disponibili: " + lista;

                socket.send(new DatagramPacket(
                        loginOK.getBytes(),
                        loginOK.length(),
                        clientAddress,
                        clientPort
                ));

                // ricezione richiesta canzone
                receivePacket =
                        new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                // togliamo l'estensione .mp3 dalla richiesta se presente
                String titolo = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                if (titolo.endsWith(".mp3")) {
                    titolo = titolo.substring(0, titolo.length() - 4);
                }

                File file = new File(titolo + ".mp3");

                if (file.exists()) {
                    String ok = "file ok";
                    socket.send(new DatagramPacket(
                            ok.getBytes(),
                            ok.length(),
                            clientAddress,
                            clientPort
                    ));

                    FileInputStream fis = new FileInputStream(file);
                    byte[] fileBytes = new byte[1024];
                    int letti = fis.read(fileBytes);

                    socket.send(new DatagramPacket(
                            fileBytes,
                            letti,
                            clientAddress,
                            clientPort
                    ));
                    fis.close();

                    System.out.println("file inviato: " + file.getName());
                } else {
                    String err = "errore: brano non trovato";
                    socket.send(new DatagramPacket(
                            err.getBytes(),
                            err.length(),
                            clientAddress,
                            clientPort
                    ));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
