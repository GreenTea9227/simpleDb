package mysqlconnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface Sql {

    Sql append(String sqlString1, Object... objects);

    Sql appendIn(String sqlString, List<Long> inList);

    long insert();

    long update();

    long delete();

    LocalDateTime selectDatetime();

    Long selectLong();

    String selectString();

    Map<String, Object> selectRow();

    <T> T selectRow(Class<T> type);

    <T> List<T> selectRows(Class<T> type);

    List<Long> selectLongs();

    void executeQuery() throws SQLException;

    Integer executeUpdate() throws SQLException;

    void setParameter(PreparedStatement pstmt) throws SQLException;

    Connection getCon();

    PreparedStatement getPstmt();

    ResultSet getRs();

    boolean isDevmode();

}


