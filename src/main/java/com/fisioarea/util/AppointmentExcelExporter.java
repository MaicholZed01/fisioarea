package com.fisioarea.util;

import com.fisioarea.model.Appuntamento;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Esporta gli appuntamenti in un file Excel .xlsx senza librerie esterne.
 *
 * Il file generato è un pacchetto Office Open XML minimale compatibile con Excel,
 * Numbers e LibreOffice.
 */
public final class AppointmentExcelExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private AppointmentExcelExporter() {
    }

    public static void exportAppointments(Path destinationFile, List<Appuntamento> appointments) {
        if (destinationFile == null) {
            throw new IllegalArgumentException("File di destinazione non valido.");
        }

        try {
            Path parent = destinationFile.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (OutputStream outputStream = Files.newOutputStream(destinationFile);
                 ZipOutputStream zip = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {

                writeZipEntry(zip, "[Content_Types].xml", contentTypesXml());
                writeZipEntry(zip, "_rels/.rels", rootRelationshipsXml());
                writeZipEntry(zip, "xl/workbook.xml", workbookXml());
                writeZipEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelationshipsXml());
                writeZipEntry(zip, "xl/styles.xml", stylesXml());
                writeZipEntry(zip, "xl/worksheets/sheet1.xml", worksheetXml(appointments));
            }

        } catch (IOException e) {
            throw new RuntimeException("Errore durante l'esportazione Excel degli appuntamenti.", e);
        }
    }

    private static void writeZipEntry(ZipOutputStream zip, String entryName, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(entryName));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypesXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                    <Default Extension="xml" ContentType="application/xml"/>
                    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                    <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                </Types>
                """;
    }

    private static String rootRelationshipsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1"
                                  Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                                  Target="xl/workbook.xml"/>
                </Relationships>
                """;
    }

    private static String workbookXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                    <sheets>
                        <sheet name="Appuntamenti" sheetId="1" r:id="rId1"/>
                    </sheets>
                </workbook>
                """;
    }

    private static String workbookRelationshipsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1"
                                  Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
                                  Target="worksheets/sheet1.xml"/>
                    <Relationship Id="rId2"
                                  Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"
                                  Target="styles.xml"/>
                </Relationships>
                """;
    }

    private static String stylesXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                    <fonts count="2">
                        <font>
                            <sz val="11"/>
                            <color theme="1"/>
                            <name val="Calibri"/>
                            <family val="2"/>
                        </font>
                        <font>
                            <b/>
                            <sz val="11"/>
                            <color rgb="FFFFFFFF"/>
                            <name val="Calibri"/>
                            <family val="2"/>
                        </font>
                    </fonts>
                    <fills count="3">
                        <fill><patternFill patternType="none"/></fill>
                        <fill><patternFill patternType="gray125"/></fill>
                        <fill>
                            <patternFill patternType="solid">
                                <fgColor rgb="FF0F766E"/>
                                <bgColor indexed="64"/>
                            </patternFill>
                        </fill>
                    </fills>
                    <borders count="2">
                        <border>
                            <left/><right/><top/><bottom/><diagonal/>
                        </border>
                        <border>
                            <left style="thin"><color rgb="FFE2E8F0"/></left>
                            <right style="thin"><color rgb="FFE2E8F0"/></right>
                            <top style="thin"><color rgb="FFE2E8F0"/></top>
                            <bottom style="thin"><color rgb="FFE2E8F0"/></bottom>
                            <diagonal/>
                        </border>
                    </borders>
                    <cellStyleXfs count="1">
                        <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
                    </cellStyleXfs>
                    <cellXfs count="3">
                        <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0"/>
                        <xf numFmtId="0" fontId="1" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1"/>
                        <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyAlignment="1">
                            <alignment wrapText="1" vertical="top"/>
                        </xf>
                    </cellXfs>
                    <cellStyles count="1">
                        <cellStyle name="Normal" xfId="0" builtinId="0"/>
                    </cellStyles>
                </styleSheet>
                """;
    }

    private static String worksheetXml(List<Appuntamento> appointments) {
        StringBuilder xml = new StringBuilder();

        xml.append("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                           xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                    <dimension ref="A1:G1"/>
                    <cols>
                        <col min="1" max="1" width="14" customWidth="1"/>
                        <col min="2" max="2" width="10" customWidth="1"/>
                        <col min="3" max="3" width="28" customWidth="1"/>
                        <col min="4" max="4" width="34" customWidth="1"/>
                        <col min="5" max="5" width="16" customWidth="1"/>
                        <col min="6" max="6" width="16" customWidth="1"/>
                        <col min="7" max="7" width="16" customWidth="1"/>
                    </cols>
                    <sheetData>
                """);

        xml.append(row(1, true, List.of(
                "Data",
                "Ora",
                "Paziente",
                "Trattamento",
                "Stato",
                "Sala",
                "Immagini"
        )));

        int rowIndex = 2;

        if (appointments != null) {
            for (Appuntamento appointment : appointments) {
                xml.append(row(rowIndex, false, List.of(
                        appointment.getDataOra().format(DATE_FORMATTER),
                        appointment.getDataOra().format(TIME_FORMATTER),
                        appointment.getPaziente(),
                        appointment.getTrattamento(),
                        appointment.getStato(),
                        appointment.getSala(),
                        String.valueOf(appointment.getNumeroImmagini())
                )));

                rowIndex++;
            }
        }

        xml.append("""
                    </sheetData>
                    <autoFilter ref="A1:G1"/>
                    <pageMargins left="0.7" right="0.7" top="0.75" bottom="0.75" header="0.3" footer="0.3"/>
                </worksheet>
                """);

        return xml.toString();
    }

    private static String row(int rowIndex, boolean header, List<String> values) {
        StringBuilder row = new StringBuilder();
        row.append("<row r=\"").append(rowIndex).append("\">");

        for (int i = 0; i < values.size(); i++) {
            String cellReference = columnName(i + 1) + rowIndex;
            row.append(cell(cellReference, values.get(i), header));
        }

        row.append("</row>");
        return row.toString();
    }

    private static String cell(String reference, String value, boolean header) {
        int style = header ? 1 : 2;

        return "<c r=\"" + reference + "\" t=\"inlineStr\" s=\"" + style + "\">"
                + "<is><t>" + escapeXml(value) + "</t></is>"
                + "</c>";
    }

    private static String columnName(int columnNumber) {
        StringBuilder columnName = new StringBuilder();
        int value = columnNumber;

        while (value > 0) {
            int remainder = (value - 1) % 26;
            columnName.insert(0, (char) ('A' + remainder));
            value = (value - 1) / 26;
        }

        return columnName.toString();
    }

    private static String escapeXml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
