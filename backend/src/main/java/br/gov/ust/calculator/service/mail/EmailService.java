package br.gov.ust.calculator.service.mail;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import br.gov.ust.calculator.config.MailProperties;
import br.gov.ust.calculator.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public void enviarComAnexo(EmailComAnexo email) {
        if (!mailProperties.isEnabled()) {
            throw new BusinessException("Envio de e-mail está desabilitado neste ambiente");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(mailProperties.getFrom());
            helper.setTo(email.getDestinatario());
            helper.setSubject(email.getAssunto());
            helper.setText(email.getCorpoHtml(), true);
            if (email.getLogoInline() != null && email.getLogoInline().length > 0) {
                helper.addInline(
                        RelatorioEmailComposer.LOGO_CID,
                        new ByteArrayResource(email.getLogoInline()),
                        email.getLogoMime() != null ? email.getLogoMime() : "image/png"
                );
            }
            helper.addAttachment(
                    email.getNomeAnexo(),
                    new ByteArrayResource(email.getAnexo()),
                    email.getMimeAnexo()
            );
            mailSender.send(mimeMessage);
            log.info("E-mail enviado para {} com anexo {}", email.getDestinatario(), email.getNomeAnexo());
        } catch (MessagingException | MailException ex) {
            log.error("Falha ao enviar e-mail para {}", email.getDestinatario(), ex);
            throw new BusinessException(
                    "Não foi possível enviar o e-mail. Verifique se o Mailpit está em execução (localhost:1025)."
            );
        }
    }
}
