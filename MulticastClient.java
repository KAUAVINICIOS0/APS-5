package chatapp.cliente;


import java.io.*;
import java.net.*;
import javax.swing.*;

public class MulticastClient {
    private static final String MULTICAST_IP = "230.0.0.1";
    private static final int PORTA = 6789;
    private MulticastSocket socket;
    private InetAddress grupo;
    private JTextArea saida;

    public MulticastClient(JTextArea saida) throws IOException {
        this.saida = saida;
        socket = new MulticastSocket(PORTA);
        grupo = InetAddress.getByName(MULTICAST_IP);
        socket.joinGroup(grupo);

        // Thread para ouvir mensagens
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                    socket.receive(pacote);
                    String mensagem = new String(pacote.getData(), 0, pacote.getLength());
                    saida.append("[Multicast] " + mensagem + "\n");
                } catch (IOException e) {
                    break;
                }
            }
        }).start();
    }

    public void enviar(String mensagem) throws IOException {
        byte[] dados = mensagem.getBytes();
        DatagramPacket pacote = new DatagramPacket(dados, dados.length, grupo, PORTA);
        socket.send(pacote);
    }

    public void fechar() throws IOException {
        socket.leaveGroup(grupo);
        socket.close();
    }
}

