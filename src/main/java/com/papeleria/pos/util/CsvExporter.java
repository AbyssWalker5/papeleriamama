package com.papeleria.pos.util;

import com.papeleria.pos.model.Product;
import com.papeleria.pos.model.Sale;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * CSV export utility.
 */
public final class CsvExporter {

    private CsvExporter() {}

    public static void exportProducts(List<Product> products, Path outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            // Write UTF-8 BOM
            writer.write('\ufeff');
            writer.write("ID,Nombre,Código de Barras,SKU,Categoría,Precio Compra,Precio Venta,Existencia,Stock Mínimo,Unidad,Estado");
            writer.newLine();

            for (Product p : products) {
                String line = String.join(",",
                        String.valueOf(p.getId()),
                        escapeCsv(p.getName()),
                        escapeCsv(p.getBarcode()),
                        escapeCsv(p.getSku()),
                        escapeCsv(p.getCategoryName()),
                        MoneyUtil.format(p.getPurchasePrice()),
                        MoneyUtil.format(p.getSalePrice()),
                        String.valueOf(p.getStock()),
                        String.valueOf(p.getMinStock()),
                        escapeCsv(p.getUnit()),
                        escapeCsv(p.isActive() ? "Activo" : "Inactivo")
                );
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void exportSales(List<Sale> sales, Path outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            // Write UTF-8 BOM
            writer.write('\ufeff');
            writer.write("ID Venta,Número Ticket,Fecha,Total,Estado,Metodo Pago");
            writer.newLine();

            for (Sale s : sales) {
                String line = String.join(",",
                        String.valueOf(s.getId()),
                        escapeCsv(s.getSaleNumber()),
                        DateUtil.formatDisplay(s.getCreatedAt()),
                        MoneyUtil.format(s.getTotal()),
                        escapeCsv(s.getStatus()),
                        escapeCsv(s.getPaymentMethod())
                );
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private static String escapeCsv(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
