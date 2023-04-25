package mysqlconnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimpleDb {

    private boolean devMode;
    private MyDatasource myDatasource;



    public SimpleDb(String url, String username, String password, String schema) {

        String mysqlUrl = "jdbc:mysql://" + url + ":3306/" + schema + "?serverTimezone=UTC";

        myDatasource = MyDatasource.of(mysqlUrl, username, password);
    }

    public void run(String sqlString) {
        Connection con = myDatasource.getConnection();

        PreparedStatement pstmt;

        try {
            pstmt = con.prepareStatement(sqlString);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        MyDatasource.closeConnection(con, pstmt, null);


    }

    public void run(String sqlString, Object... objects) {

        Connection con = myDatasource.getConnection();

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

            MyDatasource.closeConnection(con, pstmt, null);

        }


    }

    public void setDevMode(boolean mode) {
        devMode = mode;
    }

    public Sql genSql() {

        return new Sql(myDatasource.getConnection(), devMode);
    }

    public void clear() {
        myDatasource.clear();

    }


}
