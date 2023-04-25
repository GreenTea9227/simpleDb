package mysqlconnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimpleDb {
    private final String url;
    private final String username;
    private final String password;
    private final String tableName;
    private boolean devMode;


    public SimpleDb(String url, String username, String password, String schema) {

        this.username = username;
        this.password = password;
        this.tableName = schema;


        this.url = "jdbc:mysql://" + url + ":3306/" + schema + "?serverTimezone=UTC";

    }

    public void run(String sqlString) {
        Connection con = DbConnectionUtil.getConnection(url, username, password);
        PreparedStatement pstmt;

        try {
            pstmt = con.prepareStatement(sqlString);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        DbConnectionUtil.closeDb(con, pstmt, null);

    }

    public void run(String sqlString, Object... objects) {
        Connection con = DbConnectionUtil.getConnection(url, username, password);
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(sqlString);
            for (int i = 1; i <= objects.length; i++) {
                pstmt.setObject(i, objects[i - 1]);
            }

            pstmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbConnectionUtil.closeDb(con, pstmt, null);
        }


    }

    public void setDevMode(boolean mode) {
        devMode = mode;
    }

    public Sql genSql() {
        return new Sql(url, username, password, devMode);
    }


}
