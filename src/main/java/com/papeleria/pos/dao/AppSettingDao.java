package com.papeleria.pos.dao;

import com.papeleria.pos.model.AppSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppSettingDao {
    private static final Logger logger = LoggerFactory.getLogger(AppSettingDao.class);

    public List<AppSetting> getAll(Connection conn) {
        String sql = "SELECT id, key, value, description FROM app_settings";
        List<AppSetting> settings = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                settings.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all settings", e);
            throw new RuntimeException("Error fetching all settings", e);
        }
        return settings;
    }

    public AppSetting getByKey(Connection conn, String key) {
        String sql = "SELECT id, key, value, description FROM app_settings WHERE key = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching setting by key: {}", key, e);
            throw new RuntimeException("Error fetching setting by key", e);
        }
        return null;
    }

    public void update(Connection conn, AppSetting setting) {
        String sql = "UPDATE app_settings SET value = ?, description = ? WHERE key = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, setting.getValue());
            stmt.setString(2, setting.getDescription());
            stmt.setString(3, setting.getKey());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating setting: {}", setting.getKey(), e);
            throw new RuntimeException("Error updating setting", e);
        }
    }

    private AppSetting mapRow(ResultSet rs) throws SQLException {
        AppSetting setting = new AppSetting();
        setting.setId(rs.getInt("id"));
        setting.setKey(rs.getString("key"));
        setting.setValue(rs.getString("value"));
        setting.setDescription(rs.getString("description"));
        return setting;
    }
}
