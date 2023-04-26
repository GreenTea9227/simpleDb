package mysqlconnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SqlTransactionInterface<T> {

    default T logic(SqlByInterface sql) {
        T result = null;
        try {
            init(sql.getCon());
            result = sqlExecute();
            logging(sql.isDevmode(), sql.getPstmt());
            commit(sql);
        } catch (Exception e) {
            rollback(sql.getCon());
            throw new RuntimeException();
        } finally {
            closeResource(sql);
        }
        return result;
    }

    T sqlExecute() throws Exception;

    default void init(Connection con) throws SQLException {
        con.setAutoCommit(false);
    }

    default void rollback(Connection con) {
        try {
            con.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    default void commit(SqlByInterface sql) {
        try {
            sql.getCon().commit();
            sql.getCon().setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    default void closeResource(SqlByInterface sql) {
        MyDatasource.closeConnection(sql.getCon(), sql.getPstmt(), sql.getRs());
    }

    default void logging(boolean devmode, PreparedStatement pstmt) {
        if (devmode)
            System.out.println(pstmt.toString());
    }
}
