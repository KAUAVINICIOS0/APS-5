package chatapp.cliente;

import java.io.*;
import java.net.*;

public class Cliente {
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter saida;

    public Cliente(String host, int porta) throws IOException {
        socket = new Socket(host, porta);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        saida = new PrintWriter(socket.getOutputStream(), true);
    }

    public void enviarMensagem(String mensagem) {
        saida.println(mensagem);
    }

    public BufferedReader getEntrada() {
        return entrada;
    }

    public void fechar() throws IOException {
        socket.close();
    }
    public OutputStream getOutput() throws IOException {
        return socket.getOutputStream();
    }

    public InputStream getInput() throws IOException {
        return socket.getInputStream();
    }

    public PrintWriter getSaida() {
        return saida;
    }
}
