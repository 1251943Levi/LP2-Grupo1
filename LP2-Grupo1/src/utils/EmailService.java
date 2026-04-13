package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * Serviço de envio de credenciais do sistema ISSMF.
 * Todos os envios são REAIS via SMTP (Gmail / TLS 587).
 * O sistema não simula nem omite nenhum envio — se a caixa de destino
 * não existir, o servidor remoto devolve bounce, mas o sistema tentou.
 * Destinatários de cada registo:
 *   1. E-mail institucional do novo utilizador (ex: 12345@issmf.ipp.pt)
 *   2. Os 4 endereços de backup @isep.ipp.pt
 *   3. O próprio remetente issmfsistema@gmail.com (cópia de arquivo)
 */
public class EmailService {


    /** Conta Gmail do sistema — é também o remetente e cópia de arquivo. */
    private static final String EMAIL_SISTEMA = "issmfsistema@gmail.com";

    /** App Password do Gmail (gerada em myaccount.google.com → Segurança → App Passwords). */
    private static final String APP_PASSWORD = "evgp wbti enro sphx";

    /** Endereços de backup institucionais do ISEP. */
    private static final String[] EMAILS_EQUIPA = {
            "1251666@isep.ipp.pt",
            "1251943@isep.ipp.pt",
            "1220492@isep.ipp.pt",
            "1251663@isep.ipp.pt"
    };

    private EmailService() {}

    /**
     * Envia as credenciais de acesso para TODOS os destinatários obrigatórios:
     *   - o próprio utilizador (emailUtilizador)
     *   - os 4 backups @isep.ipp.pt
     *   - o e-mail do sistema (issmfsistema@gmail.com) como cópia de arquivo
     * Deve ser chamado ANTES de encriptar a password limpa.
     * @param nomeUtilizador  Nome completo do novo estudante / docente.
     * @param emailUtilizador E-mail gerado para o utilizador (ex: 12345@issmf.ipp.pt).
     * @param passLimpa       Password em texto claro — usada apenas neste envio.
     */
    public static void enviarCredenciaisTodos(String nomeUtilizador,
                                              String emailUtilizador,
                                              String passLimpa) {
        String assunto = "[ISSMF] As suas credenciais de acesso — " + nomeUtilizador;
        String corpo   = construirCorpo(nomeUtilizador, emailUtilizador, passLimpa);

        enviarUmEmail(emailUtilizador, assunto, corpo);

        for (String backup : EMAILS_EQUIPA) {
            enviarUmEmail(backup, assunto, corpo);
        }

        enviarUmEmail(EMAIL_SISTEMA, "[ARQUIVO] " + assunto, corpo);
    }

    /**
     * Envia uma nova password temporária para recuperação de conta.
     * Usa os mesmos 3 grupos de destinatários que o registo.
     * @param nomeUtilizador  Nome do utilizador (pode ser "Utilizador" se desconhecido).
     * @param emailUtilizador E-mail institucional do utilizador.
     * @param novaPassLimpa   Nova password temporária em texto claro.
     */
    public static void enviarRecuperacaoPassword(String nomeUtilizador,
                                                 String emailUtilizador,
                                                 String novaPassLimpa) {
        String assunto = "[ISSMF] Recuperação de password — " + emailUtilizador;
        String corpo =
                "Caro(a) " + nomeUtilizador + ",\n\n" +
                        "Foi solicitada a recuperação da sua conta no sistema ISSMF.\n\n" +
                        "  E-mail: " + emailUtilizador + "\n" +
                        "  Password: " + novaPassLimpa   + "\n\n" +
                        "Por favor altere a password no próximo acesso.\n\n" +
                        "Mensagem gerada automaticamente — não responda.\n" +
                        "— Sistema ISSMF";

        enviarUmEmail(emailUtilizador, assunto, corpo);
        for (String backup : EMAILS_EQUIPA) {
            enviarUmEmail(backup, assunto, corpo);
        }
        enviarUmEmail(EMAIL_SISTEMA, "[ARQUIVO] " + assunto, corpo);
    }


    /**
     * Envia um único e-mail. Regista no log de sistema o resultado (sucesso/erro).
     * A password do utilizador que está no corpo NÃO é impressa no log.
     */
    private static void enviarUmEmail(String destinatario, String assunto, String corpo) {
        try {
            Message msg = criarMensagem(destinatario);
            msg.setSubject(assunto);
            msg.setText(corpo);
            Transport.send(msg);
            System.out.println("[SISTEMA] E-mail enviado para: " + destinatario);

        } catch (MessagingException e) {
            System.err.println("[SISTEMA] ERRO ao enviar para " + destinatario
                    + " — " + e.getMessage());
        }
    }

    /** Constrói o corpo padrão de boas-vindas com as credenciais. */
    private static String construirCorpo(String nome, String email, String pass) {
        return
                "Caro(a) " + nome + ",\n\n" +
                        "A sua conta no sistema ISSMF foi criada com sucesso.\n\n" +
                        "  E-mail: " + email + "\n" +
                        "  Password: " + pass  + "\n\n" +
                        "Por favor altere a password no primeiro acesso.\n\n" +
                        "Mensagem gerada automaticamente — não responda.\n" +
                        "— Sistema ISSMF";
    }

    /**
     * Cria e autentica uma sessão SMTP (Gmail TLS 587) e devolve uma
     * MimeMessage pronta com remetente e destinatário configurados.
     */
    private static Message criarMensagem(String destinatario) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.protocols",   "TLSv1.2");

        final String appPasswordSemEspacos = APP_PASSWORD.replace(" ", "");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_SISTEMA, appPasswordSemEspacos);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(EMAIL_SISTEMA));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        return msg;
    }
}