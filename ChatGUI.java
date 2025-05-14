package chatapp.cliente;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

public class ChatGUI extends JFrame {
    private JTextArea areaMensagens;
    private JTextField campoMensagem;
    private JButton botaoEnviar;
    private Cliente cliente;
    private JButton botaoEmoji;
    private MulticastClient multicast;

    public ChatGUI(String nomeUsuario) {
        setTitle("Chat - Usuário: " + nomeUsuario);
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Área de mensagens
        areaMensagens = new JTextArea();
        areaMensagens.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaMensagens);
        add(scroll, BorderLayout.CENTER);

        // Campo de entrada + botão
        JPanel painelEntrada = new JPanel();
        painelEntrada.setLayout(new BoxLayout(painelEntrada, BoxLayout.X_AXIS));

        campoMensagem = new JTextField();
        botaoEnviar = new JButton("Enviar");

        painelEntrada.add(botaoEnviar);
        painelEntrada.add(campoMensagem);

        // Botões adicionais
        botaoEmoji = new JButton("😀");
        painelEntrada.add(botaoEmoji);

        JButton botaoArquivo = new JButton("📎");
        painelEntrada.add(botaoArquivo);

        JButton botaoMulticast = new JButton("🌐 Multicast");
        painelEntrada.add(botaoMulticast);

        JButton botaoEmail = new JButton("✉️ Email");
        painelEntrada.add(botaoEmail);

        add(painelEntrada, BorderLayout.SOUTH);

        // Ações de botões
        botaoEnviar.addActionListener(e -> enviarMensagem(nomeUsuario));
        campoMensagem.addActionListener(e -> enviarMensagem(nomeUsuario));

        botaoEmoji.addActionListener(e -> abrirPainelEmojiWindows());

        botaoArquivo.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int retorno = chooser.showOpenDialog(this);
            if (retorno == JFileChooser.APPROVE_OPTION) {
                File arquivo = chooser.getSelectedFile();
                try {
                    FileTransfer.enviarArquivo(arquivo, cliente.getSaida(), cliente.getOutput());
                    areaMensagens.append("Arquivo enviado: " + arquivo.getName() + "\n");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao enviar arquivo: " + ex.getMessage());
                }
            }
        });

        botaoMulticast.addActionListener(e -> {
            String msg = JOptionPane.showInputDialog(this, "Digite a mensagem multicast:");
            if (msg != null && !msg.isEmpty()) {
                try {
                    multicast.enviar(nomeUsuario + ": " + msg);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao enviar multicast: " + ex.getMessage());
                }
            }
        });

        try {
            multicast = new MulticastClient(areaMensagens);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao iniciar multicast: " + e.getMessage());
        }

        botaoEmail.addActionListener(e -> {
            JTextField campoDe = new JTextField();
            JPasswordField campoSenha = new JPasswordField();
            JTextField campoPara = new JTextField();
            JTextField campoAssunto = new JTextField();
            JTextArea campoMsg = new JTextArea(5, 20);

            Object[] campos = {
                "Seu e-mail:", campoDe,
                "Sua senha:", campoSenha,
                "Para:", campoPara,
                "Assunto:", campoAssunto,
                "Mensagem:", new JScrollPane(campoMsg)
            };

            int resultado = JOptionPane.showConfirmDialog(this, campos, "Enviar E-mail", JOptionPane.OK_CANCEL_OPTION);
            if (resultado == JOptionPane.OK_OPTION) {
                try {
                    EmailSender.enviarEmail(
                        campoDe.getText(),
                        new String(campoSenha.getPassword()),
                        campoPara.getText(),
                        campoAssunto.getText(),
                        campoMsg.getText()
                    );
                    JOptionPane.showMessageDialog(this, "E-mail enviado com sucesso!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao enviar e-mail: " + ex.getMessage());
                }
            }
        });

        // Conectar ao servidor
        try {
            cliente = new Cliente("localhost", 12345);
            cliente.enviarMensagem(nomeUsuario);

            new Thread(() -> {
                try {
                    String linha;
                    while ((linha = cliente.getEntrada().readLine()) != null) {
                    	if (linha.startsWith(FileTransfer.FILE_PREFIX)) {
                    	    String[] partes = linha.substring(FileTransfer.FILE_PREFIX.length()).split(":");
                    	    String nomeArquivo = partes[0];
                    	    long tamanho = Long.parseLong(partes[1]);

                    	    int opcao = JOptionPane.showConfirmDialog(
                    	        this,
                    	        "Deseja baixar o arquivo \"" + nomeArquivo + "\" (" + tamanho + " bytes)?",
                    	        "Recebimento de Arquivo",
                    	        JOptionPane.YES_NO_OPTION
                    	    );

                    	    if (opcao == JOptionPane.YES_OPTION) {
                    	        JFileChooser chooser = new JFileChooser();
                    	        chooser.setSelectedFile(new File(nomeArquivo));
                    	        int escolha = chooser.showSaveDialog(this);
                    	        if (escolha == JFileChooser.APPROVE_OPTION) {
                    	            File destino = chooser.getSelectedFile();
                    	            try {
                    	                FileTransfer.receberArquivo(nomeArquivo, tamanho, cliente.getInput(), destino);
                    	                areaMensagens.append("Arquivo salvo em: " + destino.getAbsolutePath() + "\n");
                    	            } catch (IOException ex) {
                    	                JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + ex.getMessage());
                    	            }
                    	        } else {
                    	            areaMensagens.append("Recebimento de \"" + nomeArquivo + "\" cancelado pelo usuário.\n");
                    	        }
                    	    } else {
                    	        areaMensagens.append("Arquivo \"" + nomeArquivo + "\" recusado.\n");

                    	        // Importante: consumir os bytes do arquivo para liberar o socket
                    	        byte[] buffer = new byte[4096];
                    	        long totalLido = 0;
                    	        while (totalLido < tamanho) {
                    	            int lido = cliente.getInput().read(buffer, 0, (int)Math.min(buffer.length, tamanho - totalLido));
                    	            if (lido == -1) break;
                    	            totalLido += lido;
                    	        }
                    	    }
                    	}else {
                            areaMensagens.append(linha + "\n");
                        }
                    }
                } catch (Exception e) {
                    areaMensagens.append("Conexão encerrada.\n");
                }
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar: " + ex.getMessage());
            System.exit(1);
        }

        setVisible(true);
    }

    private void enviarMensagem(String nomeUsuario) {
        String msg = campoMensagem.getText();
        if (!msg.trim().isEmpty()) {
            cliente.enviarMensagem(msg);
            campoMensagem.setText("");
        }
    }

    private void abrirPainelEmojiWindows() {
        try {
            // Garante que o campo de mensagem tenha foco
            campoMensagem.requestFocusInWindow();

            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_WINDOWS);
            robot.keyPress(KeyEvent.VK_PERIOD);
            robot.keyRelease(KeyEvent.VK_PERIOD);
            robot.keyRelease(KeyEvent.VK_WINDOWS);
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir painel de emojis: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String nome = JOptionPane.showInputDialog("Digite seu nome:");
            if (nome != null && !nome.trim().isEmpty()) {
                new ChatGUI(nome.trim());
            }
        });
    }
}
