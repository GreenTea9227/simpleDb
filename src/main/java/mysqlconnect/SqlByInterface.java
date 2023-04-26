package mysqlconnect;

import lombok.Getter;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SqlByInterface implements Sql {

    private final List<Object> list = new ArrayList<>();
    private StringBuilder sqlQuery;
    private Connection con;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private boolean devmode;

    private SqlByInterface() {
    }

    protected SqlByInterface(Connection con, boolean devmode) {
        this.con = con;
        sqlQuery = new StringBuilder();
        this.devmode = devmode;
    }

    public SqlByInterface append(String sqlString1, Object... objects) {
        sqlQuery.append(sqlString1).append(" ");
        list.addAll(Arrays.asList(objects));
        return this;
    }

    public SqlByInterface appendIn(String sqlString, List<Long> inList) {
        String str = inList.stream().map(String::valueOf).collect(Collectors.joining(", "));
        String replace = sqlString.replace("?", str);
        sqlQuery.append(replace);

        return this;
    }

    public long insert() {

        SqlTransactionInterface<Long> sql = () -> {
            executeUpdate();
            rs = pstmt.getGeneratedKeys();
            rs.next();
            return rs.getLong(1);
        };

        return sql.logic(this);
    }

    public long update() {

        SqlTransactionInterface<Long> sql = () -> Long.valueOf(executeUpdate());

        return sql.logic(this);
    }

    public long delete() {
        SqlTransactionInterface<Long> sql = () -> Long.valueOf(executeUpdate());

        return sql.logic(this);
    }

    public LocalDateTime selectDatetime() {

        SqlTransactionInterface<LocalDateTime> sql = () -> {
            executeQuery();
            rs.next();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String currentTime = rs.getString(1);
            return LocalDateTime.parse(currentTime, formatter);
        };
        return sql.logic(this);
    }

    public Long selectLong() {
        SqlTransactionInterface<Long> sql = () -> {
            executeQuery();
            rs.next();
            return rs.getLong(1);
        };
        return sql.logic(this);
    }

    public String selectString() {

        SqlTransactionInterface<String> sql = () -> {
            executeQuery();
            rs.next();
            return rs.getString(1);
        };
        return sql.logic(this);
    }

    public Map<String, Object> selectRow() {

        SqlTransactionInterface<Map<String, Object>> sql = () -> {
            Map<String, Object> map = new HashMap<>();
            executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    map.put(columnName, value);
                }
            }
            return map;
        };
        return sql.logic(this);
    }

    public <T> T selectRow(Class<T> type) {
        SqlTransactionInterface<T> sql = () -> {
            executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            rs.next();

            Object[] objects = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                objects[i - 1] = rs.getObject(i);
            }
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (constructor.getParameterTypes().length == columnCount) {
                    return (T) constructor.newInstance(objects);
                }
            }
            return null;
        };
        return sql.logic(this);
    }

    public <T> List<T> selectRows(Class<T> type) {

        SqlTransactionInterface<List<T>> sql = () -> {
            ArrayList<T> resultList = new ArrayList<>();
            executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            Constructor<?> findConstructor = null;
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (constructor.getParameterTypes().length == columnCount) {
                    findConstructor = constructor;
                    break;
                }
            }
            while (rs.next()) {
                Object[] objects = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    objects[i - 1] = rs.getObject(i);
                }
                resultList.add((T) findConstructor.newInstance(objects));
            }
            return resultList;
        };
        return sql.logic(this);
    }

    public List<Long> selectLongs() {
        SqlTransactionInterface<List<Long>> sql = () -> {
            List<Long> result = new ArrayList<>();
            executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
            return result;
        };
        return sql.logic(this);
    }

    public void executeQuery() throws SQLException {
        pstmt = con.prepareStatement(sqlQuery.toString());
        setParameter(pstmt);
        rs = pstmt.executeQuery();
    }

    public Integer executeUpdate() throws SQLException {
        pstmt = con.prepareStatement(sqlQuery.toString(), Statement.RETURN_GENERATED_KEYS);
        setParameter(pstmt);
        int num = pstmt.executeUpdate();
        return num;
    }

    public void setParameter(PreparedStatement pstmt) throws SQLException {
        for (int i = 1; i <= list.size(); i++) {
            pstmt.setObject(i, list.get(i - 1));
        }
    }
}
