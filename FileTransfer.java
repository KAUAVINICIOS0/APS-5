package chatapp.cliente;

import java.io.*;
import java.net.*;

public class FileTransfer {
    public static final String FILE_PREFIX = "[ARQUIVO]";
    
    // Envia arquivo para o servidor
    public static void enviarArquivo(File arquivo, PrintWriter saida, OutputStream output) throws IOException {
    	saida.println(FILE_PREFIX + "destinatario@" + arquivo.getName() + ":" + arquivo.length());


        byte[] buffer = new byte[4096];
        FileInputStream fis = new FileInputStream(arquivo);
        int bytesLidos;

        while ((bytesLidos = fis.read(buffer)) > 0) {
            output.write(buffer, 0, bytesLidos);
        }
        output.flush();
        fis.close();
    }

    // Recebe arquivo do servidor
    public static void receberArquivo(String nomeArquivo, long tamanho, InputStream input, File destino) throws IOException {
        FileOutputStream fos = new FileOutputStream(destino);
        byte[] buffer = new byte[4096];
        int bytesLidos;
        long totalLido = 0;

        while (totalLido < tamanho && (bytesLidos = input.read(buffer, 0, (int)Math.min(buffer.length, tamanho - totalLido))) != -1) {
            fos.write(buffer, 0, bytesLidos);
            totalLido += bytesLidos;
        }
        fos.close();
    }
}
