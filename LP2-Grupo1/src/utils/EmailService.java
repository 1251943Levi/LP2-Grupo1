package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Serviço de envio de credenciais do sistema ISSMF via SMTP (Gmail TLS 587).
 *
 * SEGURANÇA: As credenciais SMTP são lidas do ficheiro "email.properties" na raiz do projeto.
 * Este ficheiro NÃO deve ser incluído no repositório (adicionar ao .gitignore).
 *
 * Formato de email.properties:
 *   email.sistema=issmfsistema@gmail.com
 *   email.password=a_tua_app_password_aqui
 *   email.equipa.1=1251666@isep.ipp.pt
 *   email.equipa.2=1251943@isep.ipp.pt
 *   email.equipa.3=1220492@isep.ipp.pt
 *   email.equipa.4=1251663@isep.ipp.pt
 */
public class EmailService {

    private static final String EMAIL_SISTEMA;
    private static final String APP_PASSWORD;
    private static final String[] EMAILS_EQUIPA;

    static {
        Properties config = new Properties();
        File configFile = new File("email.properties");

        String emailSistema = "issmfsistema@gmail.com";
        String appPassword  = "";
        String[] emailsEquipa = {
                "1251666@isep.ipp.pt",
                "1251943@isep.ipp.pt",
                "1220492@isep.ipp.pt",
                "1251663@isep.ipp.pt"
        };

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                config.load(fis);
                emailSistema  = config.getProperty("mail.smtp.user", emailSistema);
                appPassword   = config.getProperty("mail.smtp.password", appPassword);

                for (int i = 1; i <= 4; i++) {
                    String key = "email.equipa." + i;
                    if (config.containsKey(key))
                        emailsEquipa[i - 1] = config.getProperty(key);
                }

                System.out.println("[SISTEMA] Configuração de email carregada com sucesso.");
            } catch (IOException e) {
                System.err.println("[SISTEMA] AVISO: Erro ao ler email.properties: " + e.getMessage());
                System.err.println("[SISTEMA] O envio de emails pode não funcionar corretamente.");
            }
        } else {
            System.err.println("[SISTEMA] AVISO: Ficheiro email.properties não encontrado.");
            System.err.println("[SISTEMA] Crie o ficheiro na raiz do projeto com as credenciais SMTP.");
            System.err.println("[SISTEMA] O envio de emails está desativado até o ficheiro ser criado.");
        }

        EMAIL_SISTEMA  = emailSistema;
        APP_PASSWORD   = appPassword;
        EMAILS_EQUIPA  = emailsEquipa;
    }

    private EmailService() {}

    /**
     * Envia as credenciais de acesso para o utilizador, equipa de backup e arquivo do sistema.
     * Deve ser chamado ANTES de encriptar a password limpa.
     *
     * @param nomeUtilizador  Nome completo do novo utilizador.
     * @param emailUtilizador E-mail gerado (ex: 12345@issmf.ipp.pt).
     * @param passLimpa       Password em texto claro — usada apenas neste envio.
     */
    public static void enviarCredenciaisTodos(String nomeUtilizador,
                                              String emailUtilizador,
                                              String passLimpa) {
        if (nomeUtilizador == null || emailUtilizador == null || passLimpa == null) {
            System.err.println("[SISTEMA] AVISO: Parâmetros inválidos para envio de credenciais.");
            return;
        }

        if (APP_PASSWORD.isEmpty()) {
            System.err.println("[SISTEMA] AVISO: Password SMTP não configurada. Email não enviado para " + emailUtilizador);
            return;
        }

        String assunto = "[ISSMF] As suas credenciais de acesso — " + nomeUtilizador;
        String corpo   = construirCorpo(nomeUtilizador, emailUtilizador, passLimpa);

        enviarUmEmail(emailUtilizador, assunto, corpo);
        for (String backup : EMAILS_EQUIPA)
            enviarUmEmail(backup, assunto, corpo);
        enviarUmEmail(EMAIL_SISTEMA, "[ARQUIVO] " + assunto, corpo);
    }

    /**
     * Envia uma nova password temporária para recuperação de conta.
     *
     * @param nomeUtilizador  Nome do utilizador (pode ser "Utilizador" se desconhecido).
     * @param emailUtilizador E-mail institucional do utilizador.
     * @param novaPassLimpa   Nova password temporária em texto claro.
     */
    public static void enviarRecuperacaoPassword(String nomeUtilizador,
                                                 String emailUtilizador,
                                                 String novaPassLimpa) {
        if (nomeUtilizador == null || emailUtilizador == null || novaPassLimpa == null) {
            System.err.println("[SISTEMA] AVISO: Parâmetros inválidos para recuperação de password.");
            return;
        }

        if (APP_PASSWORD.isEmpty()) {
            System.err.println("[SISTEMA] AVISO: Password SMTP não configurada. Email não enviado para " + emailUtilizador);
            return;
        }

        String assunto = "[ISSMF] Recuperação de password — " + emailUtilizador;
        String corpo   = "Caro(a) " + nomeUtilizador + ",\n\n"
                + "Foi solicitada a recuperação da sua conta no sistema ISSMF.\n\n"
                + "  E-mail: " + emailUtilizador + "\n"
                + "  Password: " + novaPassLimpa + "\n\n"
                + "Por favor altere a password no próximo acesso.\n\n"
                + "Mensagem gerada automaticamente — não responda.\n"
                + "— Sistema ISSMF";

        enviarUmEmail(emailUtilizador, assunto, corpo);
        for (String backup : EMAILS_EQUIPA)
            enviarUmEmail(backup, assunto, corpo);
        enviarUmEmail(EMAIL_SISTEMA, "[ARQUIVO] " + assunto, corpo);
    }

    private static void enviarUmEmail(String destinatario, String assunto, String corpo) {
        try {
            Message msg = criarMensagem(destinatario);
            msg.setSubject(assunto);
            msg.setText(corpo);
            Transport.send(msg);
            System.out.println("[SISTEMA] E-mail enviado para: " + destinatario);
        } catch (MessagingException e) {
            System.err.println("[SISTEMA] ERRO ao enviar para " + destinatario + " — " + e.getMessage());
        }
    }

    private static String construirCorpo(String nome, String email, String pass) {
        return "Caro(a) " + nome + ",\n\n"
                + "A sua conta no sistema ISSMF foi criada com sucesso.\n\n"
                + "  E-mail: " + email + "\n"
                + "  Password: " + pass + "\n\n"
                + "Por favor altere a password no primeiro acesso.\n\n"
                + "Mensagem gerada automaticamente — não responda.\n"
                + "— Sistema ISSMF";
    }

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
