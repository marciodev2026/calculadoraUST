package br.gov.ust.calculator.service.relatorio;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import br.gov.ust.calculator.entity.SquadMembro;

@Component
public class RelatorioExcelGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] gerar(RelatorioConteudo conteudo) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle borderStyle = createBorderStyle(workbook);

            criarAbaIdentificacao(workbook, conteudo, headerStyle, borderStyle);
            criarAbaConfiguracao(workbook, conteudo, headerStyle, borderStyle);
            criarAbaProjetos(workbook, conteudo, headerStyle, borderStyle);
            criarAbaSquadMembros(workbook, conteudo, headerStyle, borderStyle);

            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao gerar relatório Excel", ex);
        }
    }

    private void criarAbaIdentificacao(
            Workbook workbook,
            RelatorioConteudo conteudo,
            CellStyle headerStyle,
            CellStyle borderStyle
    ) {
        Sheet sheet = workbook.createSheet("Identificação");
        int rowIdx = 0;

        String orgaoNome = conteudo.getNomeOrganizacao() != null
                ? conteudo.getNomeOrganizacao()
                : "Governo Federal";
        rowIdx = addSectionTitle(sheet, rowIdx, orgaoNome, headerStyle);
        rowIdx++;

        rowIdx = addSectionTitle(sheet, rowIdx, "Dados da Simulação", headerStyle);
        rowIdx = addKeyValue(sheet, rowIdx, "Solicitante", conteudo.getSimulacao().getNomeCompleto(), borderStyle);
        rowIdx = addKeyValue(sheet, rowIdx, "E-mail", conteudo.getSimulacao().getEmail(), borderStyle);
        rowIdx = addKeyValue(sheet, rowIdx, "Órgão", conteudo.getSimulacao().getOrgao(), borderStyle);
        rowIdx = addKeyValue(sheet, rowIdx, "Departamento", conteudo.getSimulacao().getDepartamento(), borderStyle);
        rowIdx = addKeyValue(sheet, rowIdx, "Telefone", valueOrDash(conteudo.getSimulacao().getTelefone()), borderStyle);
        rowIdx = addKeyValue(sheet, rowIdx, "Data", conteudo.getSimulacao().getDataSimulacao().format(DATE_FMT), borderStyle);
        rowIdx = addKeyValue(sheet, rowIdx, "Status", conteudo.getSimulacao().getStatus().name(), borderStyle);

        rowIdx++;
        rowIdx = addSectionTitle(sheet, rowIdx, "Totais", headerStyle);
        addKeyValue(sheet, rowIdx, "Total Horas", formatNumber(conteudo.getTotalHoras()), borderStyle);
        addKeyValue(sheet, rowIdx + 1, "Total UST", formatNumber(conteudo.getTotalUst()), borderStyle);
        addKeyValue(sheet, rowIdx + 2, "Valor Total", formatCurrency(conteudo.getValorTotal()), borderStyle);

        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 12000);
    }

    private void criarAbaConfiguracao(
            Workbook workbook,
            RelatorioConteudo conteudo,
            CellStyle headerStyle,
            CellStyle borderStyle
    ) {
        Sheet sheet = workbook.createSheet("Configuração UST");
        int rowIdx = addSectionTitle(sheet, 0, "Parâmetros Vigentes", headerStyle);

        rowIdx = addKeyValue(sheet, rowIdx, "Valor UST", formatCurrency(conteudo.getConfiguracao().getValorUst()), borderStyle);
        rowIdx = addKeyValue(sheet, rowIdx, "Carga Horária Mês", conteudo.getConfiguracao().getCargaHorariaMes() + " h", borderStyle);
        rowIdx = addKeyValue(sheet, rowIdx, "Encargos", formatPercent(conteudo.getConfiguracao().getEncargosPercentual()), borderStyle);
        addKeyValue(sheet, rowIdx, "BDI", formatPercent(conteudo.getConfiguracao().getBdiPercentual()), borderStyle);

        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 8000);
    }

    private void criarAbaProjetos(
            Workbook workbook,
            RelatorioConteudo conteudo,
            CellStyle headerStyle,
            CellStyle borderStyle
    ) {
        Sheet sheet = workbook.createSheet("Projetos");
        String[] headers = {"Projeto", "Tipo", "Semanas", "H/Semana", "Status", "Horas", "UST", "Valor"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (RelatorioConteudo.ProjetoRelatorioLinha linha : conteudo.getProjetos()) {
            Row row = sheet.createRow(rowIdx++);
            createCell(row, 0, linha.getProjeto().getNome(), borderStyle);
            createCell(row, 1, linha.getProjeto().getTipo().name(), borderStyle);
            createCell(row, 2, linha.getProjeto().getSemanas(), borderStyle);
            createCell(row, 3, linha.getProjeto().getHorasSemanais(), borderStyle);
            createCell(row, 4, linha.getProjeto().getStatus().name(), borderStyle);
            createCell(row, 5, formatNumber(linha.getTotalHoras()), borderStyle);
            createCell(row, 6, formatNumber(linha.getTotalUst()), borderStyle);
            createCell(row, 7, formatCurrency(linha.getValorTotal()), borderStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void criarAbaSquadMembros(
            Workbook workbook,
            RelatorioConteudo conteudo,
            CellStyle headerStyle,
            CellStyle borderStyle
    ) {
        Sheet sheet = workbook.createSheet("Squad Membros");
        String[] headers = {"Projeto", "Perfil", "FCP", "Qtd", "Horas", "UST"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (RelatorioConteudo.ProjetoRelatorioLinha linha : conteudo.getProjetos()) {
            for (SquadMembro membro : linha.getMembros()) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, linha.getProjeto().getNome(), borderStyle);
                createCell(row, 1, membro.getPerfil().getNome(), borderStyle);
                createCell(row, 2, formatNumber(membro.getFcpAplicado()), borderStyle);
                createCell(row, 3, membro.getQuantidade(), borderStyle);
                createCell(row, 4, formatNumber(membro.getHorasCalculadas()), borderStyle);
                createCell(row, 5, formatNumber(membro.getUstCalculada()), borderStyle);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private int addSectionTitle(Sheet sheet, int rowIdx, String title, CellStyle headerStyle) {
        Row row = sheet.createRow(rowIdx);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(headerStyle);
        return rowIdx + 1;
    }

    private int addKeyValue(Sheet sheet, int rowIdx, String key, String value, CellStyle style) {
        Row row = sheet.createRow(rowIdx);
        createCell(row, 0, key, style);
        createCell(row, 1, value, style);
        return rowIdx + 1;
    }

    private void createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBorderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private String valueOrDash(String value) {
        return value != null && !value.isBlank() ? value : "—";
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

    private String formatPercent(BigDecimal value) {
        return formatNumber(value) + "%";
    }
}
