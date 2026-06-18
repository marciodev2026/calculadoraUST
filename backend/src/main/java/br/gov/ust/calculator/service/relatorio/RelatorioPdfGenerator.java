package br.gov.ust.calculator.service.relatorio;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.lowagie.text.Image;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Component
public class RelatorioPdfGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

    public byte[] gerar(RelatorioConteudo conteudo) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(document, output);
            document.open();

            if (conteudo.getLogoBytes() != null && conteudo.getLogoBytes().length > 0) {
                try {
                    Image logo = Image.getInstance(conteudo.getLogoBytes());
                    logo.scaleToFit(120, 60);
                    logo.setAlignment(Element.ALIGN_LEFT);
                    document.add(logo);
                    document.add(spacer());
                } catch (Exception ignored) {
                    // logo inválido — gera PDF sem imagem
                }
            }

            String orgaoNome = conteudo.getNomeOrganizacao() != null
                    ? conteudo.getNomeOrganizacao()
                    : "Governo Federal";

            document.add(new Paragraph("Relatório Executivo UST", TITLE_FONT));
            document.add(new Paragraph(orgaoNome + " — Simulação de Esforço e Custo", NORMAL_FONT));
            document.add(spacer());

            document.add(new Paragraph("Identificação", SUBTITLE_FONT));
            document.add(infoLine("Solicitante", conteudo.getSimulacao().getNomeCompleto()));
            document.add(infoLine("E-mail", conteudo.getSimulacao().getEmail()));
            document.add(infoLine("Órgão", conteudo.getSimulacao().getOrgao()));
            document.add(infoLine("Departamento", conteudo.getSimulacao().getDepartamento()));
            document.add(infoLine("Data da Simulação", conteudo.getSimulacao().getDataSimulacao().format(DATE_FMT)));
            document.add(infoLine("Status", conteudo.getSimulacao().getStatus().name()));
            document.add(spacer());

            document.add(new Paragraph("Resumo Executivo", SUBTITLE_FONT));
            PdfPTable resumo = new PdfPTable(2);
            resumo.setWidthPercentage(100);
            resumo.setSpacingBefore(8f);
            addResumoRow(resumo, "Total de Projetos", String.valueOf(conteudo.getProjetos().size()));
            addResumoRow(resumo, "Total de Horas", formatNumber(conteudo.getTotalHoras()));
            addResumoRow(resumo, "Total UST", formatNumber(conteudo.getTotalUst()));
            addResumoRow(resumo, "Valor Total", formatCurrency(conteudo.getValorTotal()));
            addResumoRow(resumo, "Valor UST", formatCurrency(conteudo.getConfiguracao().getValorUst()));
            document.add(resumo);
            document.add(spacer());

            document.add(new Paragraph("Projetos e Sustentações", SUBTITLE_FONT));
            PdfPTable tabela = new PdfPTable(6);
            tabela.setWidthPercentage(100);
            tabela.setSpacingBefore(8f);
            tabela.setWidths(new float[]{3f, 1.5f, 1f, 1f, 1.5f, 1.8f});

            addHeader(tabela, "Projeto");
            addHeader(tabela, "Tipo");
            addHeader(tabela, "Semanas");
            addHeader(tabela, "H/Sem");
            addHeader(tabela, "UST");
            addHeader(tabela, "Valor");

            for (RelatorioConteudo.ProjetoRelatorioLinha linha : conteudo.getProjetos()) {
                addCell(tabela, linha.getProjeto().getNome());
                addCell(tabela, formatTipo(linha.getProjeto().getTipo().name()));
                addCell(tabela, String.valueOf(linha.getProjeto().getSemanas()));
                addCell(tabela, String.valueOf(linha.getProjeto().getHorasSemanais()));
                addCell(tabela, formatNumber(linha.getTotalUst()));
                addCell(tabela, formatCurrency(linha.getValorTotal()));
            }

            document.add(tabela);
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao gerar relatório PDF", ex);
        }
    }

    private Paragraph infoLine(String label, String value) {
        return new Paragraph(label + ": " + value, NORMAL_FONT);
    }

    private Paragraph spacer() {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(10f);
        return p;
    }

    private void addResumoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setPadding(6f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setPadding(6f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new Color(30, 58, 95));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(5f);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private String formatNumber(BigDecimal value) {
        if (value == null) {
            return "0,00";
        }
        return String.format("%,.2f", value).replace(',', 'X').replace('.', ',').replace('X', '.');
    }

    private String formatCurrency(BigDecimal value) {
        return "R$ " + formatNumber(value);
    }

    private String formatTipo(String tipo) {
        return switch (tipo) {
            case "PROJETO" -> "Projeto";
            case "SUSTENTACAO" -> "Sustentação";
            case "EVOLUCAO" -> "Evolução";
            case "CORRECAO" -> "Correção";
            default -> tipo;
        };
    }
}
