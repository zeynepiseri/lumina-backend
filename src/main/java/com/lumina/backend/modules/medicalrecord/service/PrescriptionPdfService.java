package com.lumina.backend.modules.medicalrecord.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lumina.backend.modules.medicalrecord.entity.Prescription;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PrescriptionPdfService {

    public byte[] generatePrescriptionPdf(Prescription prescription) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, out);

            document.open();
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{3f, 1f});
            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            titleCell.addElement(new Paragraph("LUMINA MEDICAL CENTER", titleFont));
            titleCell.addElement(new Paragraph("Official Prescription Document", FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY)));
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(titleCell);
            PdfPCell qrCell = new PdfPCell();
            qrCell.setBorder(Rectangle.NO_BORDER);
            qrCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            String baseUrl = "https://lumina-backend-7nto.onrender.com";
            String verifyUrl = baseUrl + "/api/medical-records/prescriptions/" + prescription.getId() + "/verify";
            String qrContent = verifyUrl;
            Image qrImage = Image.getInstance(generateQrCodeImage(qrContent, 100, 100));
            qrCell.addElement(qrImage);
            headerTable.addCell(qrCell);

            document.add(headerTable);
            document.add(new Paragraph("\n"));
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(10f);
            PdfPCell doctorCell = new PdfPCell();
            doctorCell.setBorder(Rectangle.NO_BORDER);
            if (prescription.getDoctor() != null) {
                doctorCell.addElement(new Phrase("Dr. " + prescription.getDoctor().getFullName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                doctorCell.addElement(new Phrase("Dip. No: " + prescription.getDoctor().getDiplomaNo(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
                doctorCell.addElement(new Phrase("Branch: " + prescription.getDoctor().getSpecialty(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
            }
            infoTable.addCell(doctorCell);
            PdfPCell patientCell = new PdfPCell();
            patientCell.setBorder(Rectangle.NO_BORDER);
            patientCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            if (prescription.getPatient() != null) {
                patientCell.addElement(new Phrase("Patient: " + prescription.getPatient().getFullName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                patientCell.addElement(new Phrase("TC: " + prescription.getPatient().getNationalId(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
            }

            if (prescription.getStartDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                patientCell.addElement(new Phrase("Date: " + prescription.getStartDate().format(formatter), FontFactory.getFont(FontFactory.HELVETICA, 10)));
            }
            infoTable.addCell(patientCell);
            document.add(infoTable);

            document.add(new Paragraph("--------------------------------------------------------------------------------------------------"));
            Font rxFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.BLACK);
            document.add(new Paragraph("Rp.", rxFont));

            Font medNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font detailsFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("1. " + prescription.getMedicationName(), medNameFont));

            String usage = String.format("   %s | %s days | %s",
                    prescription.getDosage() != null ? prescription.getDosage() : "-",
                    prescription.getDurationInDays() != null ? prescription.getDurationInDays() : "-",
                    prescription.getFrequencyDays() != null ? prescription.getFrequencyDays() : "-");
            document.add(new Paragraph(usage, detailsFont));

            if (prescription.getInstructions() != null) {
                Font noteFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY);
                document.add(new Paragraph("   Note: " + prescription.getInstructions(), noteFont));
            }

            document.add(new Paragraph("\n\n\n\n"));
            Paragraph signature = new Paragraph("(Signature & Stamp)", FontFactory.getFont(FontFactory.HELVETICA, 10));
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF", e);
        }
    }
    private byte[] generateQrCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}