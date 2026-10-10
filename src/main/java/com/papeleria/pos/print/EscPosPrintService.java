package com.papeleria.pos.print;

import com.papeleria.pos.model.TicketContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub implementation of PrintService for future ESC/POS thermal printer support.
 */
public class EscPosPrintService implements PrintService {
    private static final Logger logger = LoggerFactory.getLogger(EscPosPrintService.class);

    @Override
    public PrintResult print(TicketContent ticket) {
        logger.warn("Se intentó imprimir en ESC/POS pero no está implementado");
        // TODO: Implement ESC/POS thermal printing
        // This will require adding a dependency like escpos-coffee or javax.print
        // and formatting the ticket content as ESC/POS byte commands
        return PrintResult.failure("ESC/POS printing not yet implemented");
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getName() {
        return "Impresora Térmica (ESC/POS)";
    }
}
