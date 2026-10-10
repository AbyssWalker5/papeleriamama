package com.papeleria.pos.print;

import com.papeleria.pos.model.TicketContent;
import com.papeleria.pos.model.TicketContent.TicketItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implements PrintService to generate a formatted text file ticket.
 */
public class TextFilePrintService implements PrintService {
    private static final Logger logger = LoggerFactory.getLogger(TextFilePrintService.class);
    private final Path outputDirectory;
    private final int charWidth = 40; // Assuming 80mm width (~40 characters)

    public TextFilePrintService(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public PrintResult print(TicketContent ticket) {
        if (ticket == null) {
            return PrintResult.failure("Ticket content is null");
        }

        try {
            if (!Files.exists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("ticket_%s_%s.txt", ticket.getSaleNumber(), timestamp);
            Path filePath = outputDirectory.resolve(fileName);

            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write(centerText(ticket.getStoreName()));
                writer.newLine();
                if (ticket.getStoreAddress() != null && !ticket.getStoreAddress().isEmpty()) {
                    writer.write(centerText(ticket.getStoreAddress()));
                    writer.newLine();
                }
                writer.write(centerText("Tel: " + ticket.getStorePhone()));
                writer.newLine();
                writer.write(getSeparator());
                writer.newLine();
                writer.write(String.format("Ticket: %s", ticket.getSaleNumber()));
                writer.newLine();
                writer.write(String.format("Fecha: %s", ticket.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                writer.newLine();
                writer.write(getSeparator());
                writer.newLine();

                writer.write(String.format("%-20s %5s %13s", "Articulo", "Cant", "Importe"));
                writer.newLine();
                writer.write(getSeparator());
                writer.newLine();

                if (ticket.getItems() != null) {
                    for (TicketItem item : ticket.getItems()) {
                        String name = item.getProductName();
                        if (name.length() > 20) name = name.substring(0, 20);
                        String qty = String.valueOf(item.getQuantity());
                        String amt = com.papeleria.pos.util.MoneyUtil.format(item.getSubtotal());
                        writer.write(String.format("%-20s %5s %13s", name, qty, amt));
                        writer.newLine();
                    }
                }

                writer.write(getSeparator());
                writer.newLine();
                writer.write(String.format("%-25s %14s", "TOTAL:", com.papeleria.pos.util.MoneyUtil.format(ticket.getTotal())));
                writer.newLine();
                
                if (ticket.getPaymentMethod() != null) {
                    writer.write(String.format("%-25s %14s", "Metodo Pago:", ticket.getPaymentMethod()));
                    writer.newLine();
                }

                writer.write(getSeparator());
                writer.newLine();
                writer.write(centerText("!Gracias por su compra!"));
                writer.newLine();
            }

            logger.info("Ticket guardado en: {}", filePath);
            return PrintResult.success();
        } catch (IOException e) {
            logger.error("Error writing ticket file", e);
            return PrintResult.failure("Error writing ticket file: " + e.getMessage());
        }
    }

    private String getSeparator() {
        return "-".repeat(charWidth);
    }

    private String centerText(String text) {
        if (text == null) text = "";
        if (text.length() >= charWidth) return text.substring(0, charWidth);
        int padding = (charWidth - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getName() {
        return "Archivo de Texto";
    }
}
