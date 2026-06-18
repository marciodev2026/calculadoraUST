package br.gov.ust.calculator.service.mail;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import br.gov.ust.calculator.entity.Simulacao;
import br.gov.ust.calculator.entity.enums.RelatorioTipo;
import br.gov.ust.calculator.service.relatorio.RelatorioConteudo;
import br.gov.ust.calculator.service.relatorio.RelatorioExportResult;

@Component
public class RelatorioEmailComposer {

    public static final String LOGO_CID = "orgLogo";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String montarAssunto(Simulacao simulacao, RelatorioTipo tipo, String assuntoPersonalizado) {
        if (assuntoPersonalizado != null && !assuntoPersonalizado.isBlank()) {
            return assuntoPersonalizado.trim();
        }
        String formato = tipo == RelatorioTipo.PDF ? "Executivo PDF" : "Técnico Excel";
        return "Relatório UST " + formato + " — " + simulacao.getOrgao();
    }

    public String montarCorpoHtml(
            RelatorioConteudo conteudo,
            RelatorioTipo tipo,
            RelatorioExportResult export,
            String mensagemPersonalizada,
            boolean possuiLogo
    ) {
        Simulacao simulacao = conteudo.getSimulacao();
        String orgao = conteudo.getNomeOrganizacao() != null ? conteudo.getNomeOrganizacao() : "Governo Federal";
        String formatoLabel = tipo == RelatorioTipo.PDF ? "PDF Executivo" : "Excel Técnico";
        String formatoBadge = tipo == RelatorioTipo.PDF ? "#dc2626" : "#16a34a";

        StringBuilder html = new StringBuilder();
        html.append("""
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#eef2f3;font-family:'Segoe UI',Arial,sans-serif;color:#1f2937;">
                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:#eef2f3;padding:32px 16px;">
                <tr><td align="center">
                <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                """);

        html.append("""
                <tr><td style="background:linear-gradient(135deg,#1e5c52 0%,#1e3a5f 100%);padding:28px 32px;">
                <table role="presentation" width="100%" cellpadding="0" cellspacing="0"><tr>
                """);

        if (possuiLogo) {
            html.append("""
                    <td width="80" valign="middle" style="padding-right:16px;">
                    <img src="cid:""").append(LOGO_CID).append("""
                    " alt="Logo" style="max-height:56px;max-width:120px;display:block;border-radius:4px;background:#ffffff;padding:4px;">
                    </td>
                    """);
        }

        html.append("<td valign=\"middle\">");
        html.append("<p style=\"margin:0;font-size:11px;letter-spacing:0.08em;text-transform:uppercase;color:rgba(255,255,255,0.75);\">")
                .append(escapeHtml(orgao)).append("</p>");
        html.append("<h1 style=\"margin:6px 0 0;font-size:22px;font-weight:700;color:#ffffff;line-height:1.3;\">")
                .append("Relatório de Simulação UST").append("</h1>");
        html.append("<p style=\"margin:8px 0 0;font-size:13px;color:rgba(255,255,255,0.85);\">")
                .append("Sistema UST Gov — Unidade de Serviço Técnico").append("</p>");
        html.append("</td></tr></table></td></tr>");

        html.append("""
                <tr><td style="padding:32px;">
                <table role="presentation" width="100%" cellpadding="0" cellspacing="0">
                <tr><td>
                <span style="display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;color:#ffffff;background:""")
                .append(formatoBadge).append(";\">").append(formatoLabel).append("</span>");
        html.append("<p style=\"margin:20px 0 0;font-size:15px;line-height:1.6;color:#374151;\">");
        html.append("Prezado(a), segue em anexo o relatório <strong>")
                .append(tipo == RelatorioTipo.PDF ? "executivo em PDF" : "técnico em Excel")
                .append("</strong> referente à simulação de esforço e custo em UST.");
        html.append("</p>");

        if (mensagemPersonalizada != null && !mensagemPersonalizada.isBlank()) {
            html.append("""
                    <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="margin:20px 0;">
                    <tr><td style="background:#f0fdfa;border-left:4px solid #1e5c52;padding:14px 18px;border-radius:0 8px 8px 0;">
                    <p style="margin:0;font-size:14px;line-height:1.6;color:#134e4a;font-style:italic;">""")
                    .append(escapeHtml(mensagemPersonalizada.trim()))
                    .append("</p></td></tr></table>");
        }

        html.append("""
                <table role="presentation" width="100%" cellpadding="0" cellspacing="0"
                       style="margin:24px 0;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;">
                <tr><td colspan="2" style="background:#f8fafc;padding:12px 20px;font-size:13px;font-weight:700;color:#1e5c52;text-transform:uppercase;letter-spacing:0.05em;">
                Dados da Simulação
                </td></tr>
                """);
        appendInfoRow(html, "Solicitante", simulacao.getNomeCompleto());
        appendInfoRow(html, "E-mail", simulacao.getEmail());
        appendInfoRow(html, "Órgão", simulacao.getOrgao());
        appendInfoRow(html, "Departamento", simulacao.getDepartamento());
        appendInfoRow(html, "Data", simulacao.getDataSimulacao().format(DATE_FMT));
        appendInfoRow(html, "Status", simulacao.getStatus().name());
        html.append("</table>");

        html.append("""
                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="margin:0 0 24px;">
                <tr>
                """);
        appendStatCell(html, formatNumber(conteudo.getTotalHoras()), "Horas", "#6366f1");
        appendStatCell(html, formatNumber(conteudo.getTotalUst()), "UST", "#d97706");
        appendStatCell(html, formatCurrency(conteudo.getValorTotal()), "Valor Total", "#1e5c52");
        html.append("</tr></table>");

        html.append("""
                <table role="presentation" width="100%" cellpadding="0" cellspacing="0">
                <tr><td style="background:#f8fafc;border:1px dashed #cbd5e1;border-radius:8px;padding:16px 20px;text-align:center;">
                <p style="margin:0;font-size:13px;color:#6b7280;">
                📎 Anexo: <strong style="color:#1f2937;">""")
                .append(escapeHtml(export.getNomeArquivo()))
                .append("""
                </strong>
                </p></td></tr></table>
                </td></tr></table>
                </td></tr>
                """);

        html.append("""
                <tr><td style="background:#f8fafc;padding:20px 32px;border-top:1px solid #e5e7eb;">
                <p style="margin:0;font-size:11px;color:#9ca3af;text-align:center;line-height:1.5;">
                Mensagem automática do <strong style="color:#6b7280;">UST Gov</strong>. Não responda este e-mail.<br>
                © Simulação de Esforço e Custo — Órgãos Governamentais
                </p></td></tr>
                </table>
                </td></tr></table>
                </body></html>
                """);

        return html.toString();
    }

    public String obterMimeAnexo(RelatorioTipo tipo) {
        return tipo == RelatorioTipo.PDF
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    public String obterMimeLogo(String logoCaminho) {
        if (logoCaminho == null) {
            return "image/png";
        }
        String nome = logoCaminho.toLowerCase();
        if (nome.endsWith(".jpg") || nome.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (nome.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/png";
    }

    private void appendInfoRow(StringBuilder html, String label, String value) {
        html.append("<tr><td style=\"padding:10px 20px;border-top:1px solid #f1f5f9;width:140px;font-size:13px;font-weight:600;color:#6b7280;\">")
                .append(escapeHtml(label))
                .append("</td><td style=\"padding:10px 20px;border-top:1px solid #f1f5f9;font-size:14px;color:#1f2937;\">")
                .append(escapeHtml(value != null ? value : "—"))
                .append("</td></tr>");
    }

    private void appendStatCell(StringBuilder html, String value, String label, String color) {
        html.append("<td width=\"33%\" style=\"padding:4px;\">")
                .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">")
                .append("<tr><td style=\"background:#f8fafc;border-radius:8px;padding:14px;text-align:center;border-top:3px solid ")
                .append(color).append(";\">")
                .append("<p style=\"margin:0;font-size:18px;font-weight:700;color:#1f2937;\">").append(value).append("</p>")
                .append("<p style=\"margin:4px 0 0;font-size:11px;color:#9ca3af;text-transform:uppercase;letter-spacing:0.05em;\">")
                .append(label).append("</p>")
                .append("</td></tr></table></td>");
    }

    private String formatNumber(BigDecimal value) {
        if (value == null) {
            return "0,00";
        }
        return String.format("%,.2f", value).replace(',', 'X').replace('.', ',').replace('X', '.');
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "R$ 0,00";
        }
        return "R$ " + formatNumber(value);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
