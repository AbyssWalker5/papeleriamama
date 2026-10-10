package com.papeleria.pos.print;

import com.papeleria.pos.model.TicketContent;

/**
 * Print service interface.
 */
public interface PrintService {
    PrintResult print(TicketContent ticket);
    boolean isAvailable();
    String getName();
}
