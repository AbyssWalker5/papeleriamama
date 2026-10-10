package com.papeleria.pos.service;

import com.papeleria.pos.dao.DatabaseManager;
import com.papeleria.pos.dao.AppSettingDao;
import com.papeleria.pos.model.AppSetting;
import com.papeleria.pos.model.TicketContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service for managing application settings and configuration.
 */
public class SettingsService {
    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);
    private final AppSettingDao settingsDAO = new AppSettingDao();

    /**
     * Retrieves a setting value by its key.
     */
    public String getSetting(String key) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            AppSetting setting = settingsDAO.getByKey(conn, key);
            return setting != null ? setting.getValue() : null;
        } catch (SQLException e) {
            logger.error("Error retrieving setting for key: {}", key, e);
            throw new RuntimeException("Failed to get setting", e);
        }
    }

    /**
     * Updates a setting value.
     */
    public void updateSetting(String key, String value) {
        logger.info("Updating setting: {}", key);
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            AppSetting setting = settingsDAO.getByKey(conn, key);
            if (setting != null) {
                setting.setValue(value);
                settingsDAO.update(conn, setting);
            }
        } catch (SQLException e) {
            logger.error("Error updating setting for key: {}", key, e);
            throw new RuntimeException("Failed to update setting", e);
        }
    }

    /**
     * Builds a partial TicketContent object populated with store information.
     */
    public TicketContent buildStoreInfoForTicket() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            TicketContent ticket = new TicketContent();
            
            AppSetting sn = settingsDAO.getByKey(conn, "store_name");
            String storeName = sn != null ? sn.getValue() : null;
            
            AppSetting sa = settingsDAO.getByKey(conn, "store_address");
            String address = sa != null ? sa.getValue() : null;
            
            AppSetting sp = settingsDAO.getByKey(conn, "store_phone");
            String phone = sp != null ? sp.getValue() : null;
            
            AppSetting sm = settingsDAO.getByKey(conn, "thank_you_message");
            String thankYouMessage = sm != null ? sm.getValue() : null;
            
            ticket.setStoreName(storeName != null ? storeName : "Papelería");
            ticket.setStoreAddress(address != null ? address : "");
            ticket.setStorePhone(phone != null ? phone : "");
            ticket.setThankYouMessage(thankYouMessage != null ? thankYouMessage : "¡Gracias por su compra!");
            
            return ticket;
        } catch (SQLException e) {
            logger.error("Error building store info for ticket", e);
            throw new RuntimeException("Failed to build ticket store info", e);
        }
    }
}
