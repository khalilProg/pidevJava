package tn.esprit.tools;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import tn.esprit.entities.Campagne;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CampagnePDFGenerator {

    public static void generatePDF(String filePath, List<Campagne> campagnes) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4.rotate()); // Landscape for better table fit
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        // Title Font
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.BLACK);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
        Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        Font tableBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

        // Header
        Paragraph title = new Paragraph("Rapport des Campagnes de Collecte - BLOODLINK", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        Paragraph date = new Paragraph("Généré le : " + dtf.format(LocalDateTime.now()), subtitleFont);
        date.setAlignment(Element.ALIGN_CENTER);
        date.setSpacingAfter(20);
        document.add(date);

        // Table
        PdfPTable table = new PdfPTable(5); // 5 columns
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Column widths
        float[] columnWidths = {2f, 3f, 2f, 1.5f, 2f};
        table.setWidths(columnWidths);

        // Table Header
        BaseColor headerColor = new BaseColor(231, 76, 60); // Bloodlink Red (#e74c3c)
        String[] headers = {"Titre", "Description", "Période", "Types de sang", "Lieux (Entités)"};

        for (String headerText : headers) {
            PdfPCell header = new PdfPCell(new Phrase(headerText, tableHeaderFont));
            header.setBackgroundColor(headerColor);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header.setPadding(10);
            header.setBorderWidth(1);
            header.setBorderColor(BaseColor.LIGHT_GRAY);
            table.addCell(header);
        }

        // Table Body
        boolean alternateRow = false;
        BaseColor altColor = new BaseColor(245, 245, 245);

        for (Campagne c : campagnes) {
            BaseColor rowColor = alternateRow ? altColor : BaseColor.WHITE;

            // Titre
            PdfPCell cell1 = new PdfPCell(new Phrase(c.getTitre(), tableBodyFont));

            // Description (truncate if too long)
            String desc = c.getDescription();
            if (desc != null && desc.length() > 50) {
                desc = desc.substring(0, 47) + "...";
            }
            PdfPCell cell2 = new PdfPCell(new Phrase(desc != null ? desc : "", tableBodyFont));

            // Période
            String periode = c.getDateDebut().toString() + " au " + c.getDateFin().toString();
            PdfPCell cell3 = new PdfPCell(new Phrase(periode, tableBodyFont));

            // Types de sang
            PdfPCell cell4 = new PdfPCell(new Phrase(c.getTypeSang(), tableBodyFont));

            // Entités
            String nbEntites = c.getEntiteDeCollectes() != null ? String.valueOf(c.getEntiteDeCollectes().size()) + " lieu(x)" : "0";
            PdfPCell cell5 = new PdfPCell(new Phrase(nbEntites, tableBodyFont));

            // Apply styles to all cells in the row
            PdfPCell[] cells = {cell1, cell2, cell3, cell4, cell5};
            for (PdfPCell cell : cells) {
                cell.setBackgroundColor(rowColor);
                cell.setPadding(8);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            alternateRow = !alternateRow;
        }

        document.add(table);

        // Footer
        Paragraph footer = new Paragraph("Document généré automatiquement par le système BloodLink.", subtitleFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        document.close();
    }
}
