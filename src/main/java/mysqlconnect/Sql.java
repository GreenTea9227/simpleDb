package mysqlconnect;

import lombok.Getter;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Sql {

    private StringBuilder sqlQuery;
    private Connection con;
    private PreparedStatement pstmt;
    private ResultSet rs;

    private boolean devmode;

    private final List<Object> list = new ArrayList<>();

    private Sql() {
    }

    protected Sql(Connection con, boolean devmode) {
        this.con = con;
        sqlQuery = new StringBuilder();
        this.devmode = devmode;
    }

    public Sql append(String sqlString1, Object... objects) {
        sqlQuery.append(sqlString1).append(" ");
        list.addAll(Arrays.asList(objects));
        return this;
    }

    public Sql appendIn(String sqlString, List<Long> inList) {
        String str = inList.stream().map(String::valueOf).collect(Collectors.joining(", "));
        String replace = sqlString.replace("?", str);
        sqlQuery.append(replace);

        return this;
    }

    public long insert() {
        SqlTransaction<Long> sql = new SqlTransaction<>(this) {
            @Override
            public Long sqlExecute() throws SQLException {
                executeUpdate();
                rs = pstmt.getGeneratedKeys();
                rs.next();
                return rs.getLong(1);
            }
        };
        return sql.logic();
    }

    public long update() {

        SqlTransaction<Long> sql = new SqlTransaction<>(this) {
            @Override
            public Long sqlExecute() throws SQLException {
                return Long.valueOf(executeUpdate());
            }
        };
        return sql.logic();
    }

    public long delete() {
        SqlTransaction<Long> sql = new SqlTransaction<>(this) {
            @Override
            public Long sqlExecute() throws SQLException {
                return Long.valueOf(executeUpdate());
            }
        };
        return sql.logic();
    }

    public LocalDateTime selectDatetime() {

        SqlTransaction<LocalDateTime> sql = new SqlTransaction<>(this) {
            @Override
            public LocalDateTime sqlExecute() throws SQLException {
                executeQuery();
                rs.next();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String currentTime = rs.getString(1);
                return LocalDateTime.parse(currentTime, formatter);
            }
        };
        return sql.logic();
    }

    public Long selectLong() {
        SqlTransaction<Long> sql = new SqlTransaction<>(this) {
            @Override
            public Long sqlExecute() throws SQLException {
                executeQuery();
                rs.next();
                return rs.getLong(1);
            }
        };
        return sql.logic();
    }

    public String selectString() {
        SqlTransaction<String> sql = new SqlTransaction<>(this) {
            @Override
            public String sqlExecute() throws SQLException {
                executeQuery();
                rs.next();
                return rs.getString(1);
            }
        };
        return sql.logic();
    }

    public Map<String, Object> selectRow() {
        SqlTransaction<Map<String, Object>> sql = new SqlTransaction<>(this) {
            @Override
            public Map<String, Object> sqlExecute() throws SQLException {
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
            }
        };
        return sql.logic();
    }

    public <T> T selectRow(Class<T> type) {
        SqlTransaction<T> sql = new SqlTransaction<>(this) {
            @Override
            public T sqlExecute() throws Exception {
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
            }
        };
        return sql.logic();
    }

    public <T> List<T> selectRows(Class<T> type) {
        SqlTransaction<List<T>> sql = new SqlTransaction<>(this) {
            @Override
            public List<T> sqlExecute() throws Exception {
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

            }
        };
        return sql.logic();
    }

    public List<Long> selectLongs() {
        SqlTransaction<List<Long>> sql = new SqlTransaction<>(this) {
            @Override
            public List<Long> sqlExecute() throws SQLException {
                List<Long> result = new ArrayList<>();
                executeQuery();
                while (rs.next()) {
                    result.add(rs.getLong(1));
                }
                return result;
            }
        };
        return sql.logic();
    }

    private void executeQuery() throws SQLException {
        pstmt = con.prepareStatement(sqlQuery.toString());
        setParameter(pstmt);
        rs = pstmt.executeQuery();
    }

    private Integer executeUpdate() throws SQLException {
        pstmt = con.prepareStatement(sqlQuery.toString(), Statement.RETURN_GENERATED_KEYS);
        setParameter(pstmt);
        int num = pstmt.executeUpdate();
        return num;
    }

    private void setParameter(PreparedStatement pstmt) throws SQLException {
        for (int i = 1; i <= list.size(); i++) {
            pstmt.setObject(i, list.get(i - 1));
        }
    }
}
