package my.projects.java;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

public class ConnectionsToDB {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final String CONNECTIONS_TO_DB_ERROR_IN_SQL = "ConnectionsToDB Error {}. Error in sql: {}";
    private static final String SQL = "sql: {}";
    private final PropertiesDTO propertiesDTO;
    private Connection connection = null;

    protected ConnectionsToDB(PropertiesDTO propertiesDTO) {
        this.propertiesDTO = propertiesDTO;
    }

    protected Map<Long, Boolean> getIdAndIsAvailableFromUsersTableFromDB() {
        String sql = "SELECT id, is_available FROM users_table";
        LOGGER.debug(SQL, sql);
        Map<Long, Boolean> usersMap = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                boolean isAvailable = resultSet.getBoolean("is_available");
                LOGGER.debug("id: " + id + ", isAvailable: " + isAvailable);
                usersMap.put(id, isAvailable);
            }
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 1, e.getMessage());
        }

        return usersMap;
    }

    protected boolean updateUsersTable(long userId, boolean isAvailable, String timestamp, String userName) {
        String sql = String.format("INSERT INTO users_table (id, is_available, last_action_time, name) VALUES (%s,%s,'%s','%s') ON CONFLICT(id) DO UPDATE SET is_available=%s, last_action_time='%s', name='%s'", userId, isAvailable, timestamp, userName, isAvailable, timestamp, userName);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            LOGGER.debug("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 2, e.getMessage());
        }

        return false;
    }

    protected boolean updateLastActionTimeInUsersTableByUserId(long userId, String timestamp) {
        String sql = String.format("UPDATE users_table SET last_action_time='%s' WHERE id = %s", timestamp, userId);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            LOGGER.debug("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 3, e.getMessage());
        }

        return false;
    }

    protected String getLastActionTimeInUsersTableByUserId(long userId) {
        String sql = String.format("SELECT last_action_time FROM users_table WHERE id = %s", userId);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String timestamp = resultSet.getString("last_action_time");
                LOGGER.debug("lastActionTimeFromDB: " + timestamp);
                return timestamp;
            }
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 4, e.getMessage());
        }

        return StringUtils.EMPTY;
    }

    protected Map<Integer, String> getDistrictsTable() {
        String sql = "SELECT id,name FROM districts_table";
        LOGGER.debug(SQL, sql);

        return getSimpleTable(sql);
    }

    protected Set<String> getDistrictsByUserId(long userId) {
        String sql = String.format("SELECT user_id, name FROM user_districts_table JOIN districts_table ON user_districts_table.districts_id=districts_table.id where user_id=%s", userId);
        LOGGER.debug(SQL, sql);
        Set<String> userDistrictsSet = new TreeSet<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                LOGGER.debug("name: " + name);
                userDistrictsSet.add(name);
            }
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 7, e.getMessage());
        }

        return userDistrictsSet;
    }

    protected long getDistrictIdByNameFromDistrictsTable(String districtName) {
        String sql = String.format("select id from districts_table where name='%s'", districtName);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                long id = resultSet.getLong("id");
                LOGGER.debug("id: " + id);
                return id;
            }
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 8, e.getMessage());
        }

        return 0;
    }

    protected boolean addNewToUserDistrictsTable(long userId, String districtName) {
        long districtId = getDistrictIdByNameFromDistrictsTable(districtName);
        if (districtId == 0) {
            LOGGER.debug("districtId 0");
            return false;
        }
        String sql = String.format("INSERT INTO user_districts_table VALUES (%s,%s)", userId, districtId);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            LOGGER.debug("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 9, e.getMessage());
        }

        return false;
    }

    protected boolean removeDistrictIdFromUserDistrictsTable(long userId, String districtName) {
        long districtId = getDistrictIdByNameFromDistrictsTable(districtName);
        if (districtId == 0) {
            LOGGER.debug("districtId 0");
            return false;
        }
        String sql = String.format("DELETE FROM user_districts_table WHERE (user_id=%s and districts_id=%s)", userId, districtId);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            LOGGER.debug("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 10, e.getMessage());
        }

        return false;
    }

    protected boolean removeAllDistrictsIdFromUserDistrictsTable(long userId) {
        String sql = String.format("DELETE FROM user_districts_table WHERE (user_id=%s)", userId);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            LOGGER.debug("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 11, e.getMessage());
        }

        return false;
    }

    protected boolean addAllDistrictsIdFromUserDistrictsTable(Set<Integer> districtIdsFromDistrictsTable, long userId) {
        StringBuilder createSql = new StringBuilder("INSERT INTO user_districts_table VALUES ");
        for (long s : districtIdsFromDistrictsTable) {
            createSql.append(String.format("(%s,%s),", userId, s));
        }
        createSql.deleteCharAt(createSql.length() - 1);//remove last ','

        String sql = createSql.toString();
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            LOGGER.debug("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 12, e.getMessage());
        }

        return false;
    }

    protected Set<String> getBenefitNamesFromBenefitsTable() {
        String sql = "SELECT name FROM benefits_table";
        LOGGER.debug(SQL, sql);
        Set<String> benefitsSet = new LinkedHashSet<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                LOGGER.debug("name: " + name);
                benefitsSet.add(name);
            }
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 13, e.getMessage());
        }

        return benefitsSet;
    }

    protected Map<Integer, String> getBenefitsTable() {
        String sql = "SELECT id,name FROM benefits_table";
        LOGGER.debug(SQL, sql);

        return getSimpleTable(sql);
    }

    protected long getBenefitIdByNameFromBenefitsTable(String benefitName) {
        String sql = String.format("select id from benefits_table where name='%s'", benefitName);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                long id = resultSet.getLong("id");
                LOGGER.debug("id: " + id);
                return id;
            }
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 15, e.getMessage());
        }

        return 0;
    }

    protected boolean addNewToUserSubscriptionsTable(long userId, String subscriptionName, String benefitName) {
        long benefitId = getBenefitIdByNameFromBenefitsTable(benefitName);
        if (benefitId == 0) {
            LOGGER.debug("benefitId 0");
            return false;
        }
        String sql = String.format("INSERT INTO user_subscriptions_table VALUES (%s,'%s',%s)", userId, subscriptionName, benefitId);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            LOGGER.debug("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 16, e.getMessage());
        }

        return false;
    }

    protected boolean removeFromUserSubscriptionsTableByUserId(long userId, String subscriptionName) {
        String sql = String.format("DELETE FROM user_subscriptions_table WHERE (user_id=%s and subscriptions_name='%s')", userId, subscriptionName);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            LOGGER.debug("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 17, e.getMessage());
        }

        return false;
    }

    protected boolean removeAllFromUserSubscriptionsTableByUserId(long userId) {
        String sql = String.format("DELETE FROM user_subscriptions_table WHERE (user_id=%s)", userId);
        LOGGER.debug(SQL, sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            LOGGER.debug("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 18, e.getMessage());
        }

        return false;
    }

    protected Map<String, String> getSubscriptionsMapByUserId(long userId) {
        String sql = String.format("SELECT subscriptions_name, name FROM user_subscriptions_table JOIN benefits_table ON user_subscriptions_table.benefit_id=benefits_table.id where user_id=%s", userId);
        LOGGER.debug(SQL, sql);
        Map<String, String> userSubscriptionsMap = new TreeMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String subscriptionsName = resultSet.getString("subscriptions_name");
                String name = resultSet.getString("name");
                LOGGER.debug("subscriptionsName: " + subscriptionsName + "; name: " + name);
                userSubscriptionsMap.put(subscriptionsName, name);
            }
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 19, e.getMessage());
        }

        return userSubscriptionsMap;
    }

    private Map<Integer, String> getSimpleTable(String sql) {
        Map<Integer, String> resultMap = new TreeMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                LOGGER.debug("id: " + id + " name: " + name);
                resultMap.put(id, name);
            }
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 14, e.getMessage());
        }
        return resultMap;
    }

    protected boolean createConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(propertiesDTO.getDatabaseUrl());
            if (connection == null || connection.isClosed()) {
                LOGGER.debug("Failed to make a connection!");
                return false;
            }
            LOGGER.debug("Connected to the database!");

            connection.setSchema("mydatabase");

            return true;
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 20, e.getMessage());
        }
        return false;
    }

    protected boolean closeConnection() {
        try {
            connection.close();
            LOGGER.debug("Connection closed!");
        } catch (SQLException e) {
            LOGGER.error(CONNECTIONS_TO_DB_ERROR_IN_SQL, 21, e.getMessage());
            return false;
        }
        return true;
    }

    protected boolean isConnectionActive() {
        try {
            if (connection.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
}
