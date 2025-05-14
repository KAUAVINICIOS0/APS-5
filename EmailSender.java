package chatapp.cliente;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    public static void enviarEmail(String de, String senha, String para, String assunto, String mensagem) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.office365.com"); // servidor SMTP da Microsoft
        props.put("mail.smtp.port", "587");

        Session sessao = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(de, senha);
            }
        });

        Message msg = new MimeMessage(sessao);
        msg.setFrom(new InternetAddress(de));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(para));
        msg.setSubject(assunto);
        msg.setText(mensagem);

        Transport.send(msg);
    }
}
