package mysqlconnect;

import java.sql.SQLException;

public abstract class SqlTransaction<T> {

    private final Sql sql;

    public SqlTransaction(Sql sql) {
        this.sql = sql;
    }

    public T logic() {
        T result = null;
        try {
            init();
            result = sqlExecute();
            logging();
            commit();
        } catch (Exception e) {
            rollback();
            throw new RuntimeException();
        } finally {
            closeResource();
        }
        return result;
    }

    public abstract T sqlExecute() throws Exception;

    private void init() throws SQLException {
        sql.getCon().setAutoCommit(false);
    }

    private void rollback() {
        try {
            sql.getCon().rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void commit() {
        try {
            sql.getCon().commit();
            sql.getCon().setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeResource() {
        MyDatasource.closeConnection(sql.getCon(), sql.getPstmt(), sql.getRs());
    }

    private void logging() {
        if (sql.isDevmode())
            System.out.println(sql.getPstmt().toString());
    }
}
