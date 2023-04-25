package mysqlconnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class MyDatasource {

    private static final int MAX_SIZE = 5;
    private static Map<Connection, Boolean> map;
    private static int connectionPool = MAX_SIZE;
    private static int time = 10;
    private String url;
    private String password;
    private String username;

    private MyDatasource(String url, String username, String password) {
        this.url = url;
        this.password = password;
        this.username = username;
        map = new HashMap<>();
        for (int i = 1; i <= MAX_SIZE; i++) {
            map.put(DbConnectionUtil.getConnection(url, username, password), true);
        }

    }

    public static MyDatasource of(String url, String username, String password) {
        return new MyDatasource(url, username, password);
    }

    public synchronized static void closeConnection(Connection connection, PreparedStatement pstmt, ResultSet rs) {

        DbConnectionUtil.closeResultSet(rs);
        DbConnectionUtil.closePstmt(pstmt);
        map.put(connection, true);
        connectionPool++;
    }

    public synchronized Connection getConnection() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < time) {
            for (Map.Entry<Connection, Boolean> entry : map.entrySet()) {
                if (entry.getValue()) {
                    Connection connection = entry.getKey();
                    map.put(connection, false);
                    connectionPool--;
                    return connection;
                }
            }
        }

        throw new NoConnection("가능한 커넥션이 없습니다.");
    }

    public void clear() {
        for (Connection connection : map.keySet()) {
            DbConnectionUtil.closeConnection(connection);
        }

    }


}
