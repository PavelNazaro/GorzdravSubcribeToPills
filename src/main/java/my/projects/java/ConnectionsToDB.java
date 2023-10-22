package my.projects.java;

import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;

import static my.projects.java.Main.printToLog;

public class ConnectionsToDB {
    private static final String CONNECTIONS_TO_DB_ERROR_IN_SQL = "ConnectionsToDB Error %s. Error in sql: ";
    private static final String SQL = "sql: ";
    private final PropertiesDTO propertiesDTO;
    private Connection connection = null;

    public ConnectionsToDB(PropertiesDTO propertiesDTO) {
        this.propertiesDTO = propertiesDTO;
    }

    protected Map<Long, Boolean> getIdAndIsAvailableFromUsersTableFromDB() {
        String sql = "SELECT id, is_available FROM users_table";
        printToLog(SQL + sql);
        Map<Long, Boolean> usersMap = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                boolean isAvailable = resultSet.getBoolean("is_available");
                printToLog("id: " + id + ", isAvailable: " + isAvailable);
                usersMap.put(id, isAvailable);
            }
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 1) + e.getMessage());
        }

        return usersMap;
    }

    public boolean updateUsersTable(long userId, boolean isAvailable, String timestamp, String userName) {
        String sql = String.format("INSERT INTO users_table (id, is_available, last_action_time, name) VALUES (%s,%s,'%s','%s') ON DUPLICATE KEY UPDATE is_available=%s, last_action_time='%s', name='%s'", userId, isAvailable, timestamp, userName, isAvailable, timestamp, userName);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            printToLog("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 2) + e.getMessage());
        }

        return false;
    }

    public boolean updateLastActionTimeInUsersTableByUserId(long userId, String timestamp) {
        String sql = String.format("UPDATE users_table SET last_action_time='%s' WHERE id = %s", timestamp, userId);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            printToLog("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 3) + e.getMessage());
        }

        return false;
    }

    public String getLastActionTimeInUsersTableByUserId(long userId) {
        String sql = String.format("SELECT last_action_time FROM users_table WHERE id = %s", userId);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String timestamp = resultSet.getString("last_action_time");
                printToLog("lastActionTimeFromDB: " + timestamp);
                return timestamp;
            }
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 4) + e.getMessage());
        }

        return StringUtils.EMPTY;
    }

    public Set<String> getDistrictNamesFromDistrictsTable() {
        String sql = "SELECT name FROM districts_table";
        printToLog(SQL + sql);
        Set<String> districtsSet = new TreeSet<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                printToLog("name: " + name);
                districtsSet.add(name);
            }
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 5) + e.getMessage());
        }

        return districtsSet;
    }

    public Set<Long> getDistrictIdsFromDistrictsTable() {
        String sql = "SELECT id FROM districts_table";
        printToLog(SQL + sql);
        Set<Long> districtsSet = new TreeSet<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                printToLog("id: " + id);
                districtsSet.add(id);
            }
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 6) + e.getMessage());
        }

        return districtsSet;
    }

    public Set<String> getDistrictsByUserId(long userId) {
        String sql = String.format("SELECT user_id, name FROM user_districts_table JOIN districts_table ON user_districts_table.districts_id=districts_table.id where user_id=%s", userId);
        printToLog(SQL + sql);
        Set<String> userDistrictsSet = new TreeSet<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                printToLog("name: " + name);
                userDistrictsSet.add(name);
            }
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 7) + e.getMessage());
        }

        return userDistrictsSet;
    }

    public long getDistrictIdByNameFromDistrictsTable(String districtName) {
        String sql = String.format("select id from districts_table where name='%s'", districtName);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                long id = resultSet.getLong("id");
                printToLog("id: " + id);
                return id;
            }
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 8) + e.getMessage());
        }

        return 0;
    }

    public boolean addNewToUserDistrictsTable(long userId, String districtName) {
        long districtId = getDistrictIdByNameFromDistrictsTable(districtName);
        if (districtId == 0) {
            printToLog("districtId 0");
            return false;
        }
        String sql = String.format("INSERT INTO user_districts_table VALUES (%s,%s)", userId, districtId);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            printToLog("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 9) + e.getMessage());
        }

        return false;
    }

    public boolean removeDistrictIdFromUserDistrictsTable(long userId, String districtName) {
        long districtId = getDistrictIdByNameFromDistrictsTable(districtName);
        if (districtId == 0) {
            printToLog("districtId 0");
            return false;
        }
        String sql = String.format("DELETE FROM user_districts_table WHERE (user_id=%s and districts_id=%s)", userId, districtId);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            printToLog("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 10) + e.getMessage());
        }

        return false;
    }

    public boolean removeAllDistrictsIdFromUserDistrictsTable(long userId) {
        String sql = String.format("DELETE FROM user_districts_table WHERE (user_id=%s)", userId);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            printToLog("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 11) + e.getMessage());
        }

        return false;
    }

    public boolean addAllDistrictsIdFromUserDistrictsTable(long userId) {
        Set<Long> districtIdsFromDistrictsTable = getDistrictIdsFromDistrictsTable();
        if (districtIdsFromDistrictsTable == null || districtIdsFromDistrictsTable.isEmpty()) {
            return false;
        }

        StringBuilder createSql = new StringBuilder("INSERT INTO user_districts_table VALUES ");
        for (long s : districtIdsFromDistrictsTable) {
            createSql.append(String.format("(%s,%s),", userId, s));
        }
        createSql.deleteCharAt(createSql.length() - 1);//remove last ','

        String sql = createSql.toString();
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            printToLog("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 12) + e.getMessage());
        }

        return false;
    }

    public Set<String> getBenefitNamesFromBenefitsTable() {
        String sql = "SELECT name FROM benefits_table";
        printToLog(SQL + sql);
        Set<String> benefitsSet = new LinkedHashSet<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                printToLog("name: " + name);
                benefitsSet.add(name);
            }
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 13) + e.getMessage());
        }

        return benefitsSet;
    }

    public Map<Integer, String> getBenefitsTable() {
        String sql = "SELECT * FROM benefits_table";
        printToLog(SQL + sql);
        Map<Integer, String> benefitsMap = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                printToLog("id: " + id + " name: " + name);
                benefitsMap.put(id, name);
            }
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 14) + e.getMessage());
        }
        return benefitsMap;
    }

    public long getBenefitIdByNameFromBenefitsTable(String benefitName) {
        String sql = String.format("select id from benefits_table where name='%s'", benefitName);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                long id = resultSet.getLong("id");
                printToLog("id: " + id);
                return id;
            }
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 15) + e.getMessage());
        }

        return 0;
    }

    public boolean addNewToUserSubscriptionsTable(long userId, String subscriptionName, String benefitName) {
        long benefitId = getBenefitIdByNameFromBenefitsTable(benefitName);
        if (benefitId == 0) {
            printToLog("benefitId 0");
            return false;
        }
        String sql = String.format("INSERT INTO user_subscriptions_table VALUES (%s,'%s',%s)", userId, subscriptionName, benefitId);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            printToLog("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 16) + e.getMessage());
        }

        return false;
    }

    public boolean removeFromUserSubscriptionsTableByUserId(long userId, String subscriptionName) {
        String sql = String.format("DELETE FROM user_subscriptions_table WHERE (user_id=%s and subscriptions_name='%s')", userId, subscriptionName);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            printToLog("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 17) + e.getMessage());
        }

        return false;
    }

    public boolean removeAllFromUserSubscriptionsTableByUserId(long userId) {
        String sql = String.format("DELETE FROM user_subscriptions_table WHERE (user_id=%s)", userId);
        printToLog(SQL + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = statement.executeUpdate();
            printToLog("i: " + i);
            return i >= 1;
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 18) + e.getMessage());
        }

        return false;
    }

    public Map<String, String> getSubscriptionsMapByUserId(long userId) {
        String sql = String.format("SELECT subscriptions_name, name FROM user_subscriptions_table JOIN benefits_table ON user_subscriptions_table.benefit_id=benefits_table.id where user_id=%s", userId);
        printToLog(SQL + sql);
        Map<String, String> userSubscriptionsMap = new TreeMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String subscriptionsName = resultSet.getString("subscriptions_name");
                String name = resultSet.getString("name");
                printToLog("subscriptionsName: " + subscriptionsName + " name: " + name);
                userSubscriptionsMap.put(subscriptionsName, name);
            }
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 19) + e.getMessage());
        }

        return userSubscriptionsMap;
    }

    protected boolean createConnection() {
        try {
            connection = DriverManager.getConnection(
                    propertiesDTO.getDatabaseUrl(),
                    propertiesDTO.getDatabaseUsername(),
                    propertiesDTO.getDatabasePassword());
            if (connection == null || connection.isClosed()) {
                printToLog("Failed to make a connection!");
                return false;
            }
            printToLog("Connected to the database!");

            connection.setSchema("mydatabase");

            return true;
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 20) + e.getMessage());
        }
        return false;
    }

    protected boolean closeConnection() {
        try {
            connection.close();
            printToLog("Connection closed!");
        } catch (SQLException e) {
            printToLog(String.format(CONNECTIONS_TO_DB_ERROR_IN_SQL, 21) + e.getMessage());
            return false;
        }
        return true;
    }
}
