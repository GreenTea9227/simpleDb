package mysqlconnect;


import java.lang.reflect.Constructor;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class Sql {



    private StringBuilder sqlQuery;


    private Connection con;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private boolean devmode;

    private List<Object> list = new ArrayList<>();

    private Sql() {

    }

    protected Sql(Connection con, boolean devmode) {
        this.con = con;
        sqlQuery = new StringBuilder();

        this.devmode = devmode;

    }

    public Sql append(String sqlString) {
        sqlQuery.append(sqlString).append(" ");
        return this;
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

    private void init() {
        pstmt = null;
        rs = null;
    }

    public long insert() {
        init();
        int num = -1;
        try {
            executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                num = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return num;
    }

    public long update() {
        init();
        long num = 0;
        try {
            num = executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return num;
    }

    public long delete() {
        init();
        long num = 0;
        try {
            num = executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return num;
    }

    public LocalDateTime selectDatetime() {
        init();
        LocalDateTime time = null;
        try {
            executeQuery();
            rs.next();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String currentTime = rs.getString(1);
            time = LocalDateTime.parse(currentTime, formatter);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return time;
    }

    public Long selectLong() {
        init();
        long num = 0;
        try {
            executeQuery();
            rs.next();
            num = rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return num;
    }

    public String selectString() {
        init();
        String result = "";
        try {
            executeQuery();
            rs.next();
            result = rs.getString(1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return result;
    }

    public Map<String, Object> selectRow() {
        init();
        Map<String, Object> map = new HashMap<>();
        try {
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

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return map;
    }

    public <T> T selectRow(Class<T> type) {
        init();
        T t = null;
        try {
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
                    t = (T) constructor.newInstance(objects);
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return t;
    }

    public <T> List<T> selectRows(Class<T> type) {
        init();
        ArrayList<T> list = new ArrayList<>();
        try {
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
                list.add((T) findConstructor.newInstance(objects));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return list;

    }

    public List<Long> selectLongs() {
        init();
        List<Long> result = new ArrayList<>();
        try {
            executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return result;
    }


    private void executeQuery() throws SQLException {
        pstmt = con.prepareStatement(sqlQuery.toString());
        setParameter(pstmt);
        rs = pstmt.executeQuery();
        logging();
    }


    private Integer executeUpdate() throws SQLException {
        pstmt = con.prepareStatement(sqlQuery.toString(), Statement.RETURN_GENERATED_KEYS);
        setParameter(pstmt);
        int num = pstmt.executeUpdate();
        logging();
        return num;
    }


    private void setParameter(PreparedStatement pstmt) throws SQLException {
        for (int i = 1; i <= list.size(); i++) {
            pstmt.setObject(i, list.get(i - 1));
        }
    }


    private void logging() {
        if (devmode)
            System.out.println(pstmt.toString());
    }


    private void close() {
        MyDatasource.closeConnection(con, pstmt, rs);
    }

}
