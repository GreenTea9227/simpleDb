package mysqlconnect;

import java.sql.*;

public class DbConnectionUtil {

    public static Connection getConnection(String url, String username, String password) {
        Connection con;

        try {

            con = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return con;
    }

    public static void closeDb(Connection con, PreparedStatement pstmt, ResultSet rs) {
        try {
            closeResultSet(rs);
            closePstmt(pstmt);
            closeConnection(con);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void closeConnection(Connection con) throws SQLException {
        if (con != null)
            con.close();
    }

    private static void closePstmt(PreparedStatement pstmt) throws SQLException {
        if (pstmt != null)
            pstmt.close();
    }

    private static void closeResultSet(ResultSet rs) throws SQLException {
        if (rs != null)
            rs.close();
    }


}
