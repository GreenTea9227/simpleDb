# simpleDb

## 1. DbConnectionUtil

### Db의 연결을 담당하는 클래스입니다.
```java
public static Connection getConnection(String url, String username, String password) {
        Connection con;

        try {

            con = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return con;
    }
    
  public static void closeAll(Connection con, PreparedStatement pstmt, ResultSet rs) {

        closeResultSet(rs);
        closePstmt(pstmt);
        closeConnection(con);

    }
```
>
**getConnection** : 실제 **db**의 연결을 담당하는 메소드입니다.
>
**closeConnection** : **closeConnection**는 커넥션을 끊는 메소드입니다.
>
**closePstmt** : **closePstmt**는 prepareStatement를 끊는 메소드입니다.
>
**closeResultSet** :  **closeResultSet**는 ResultSet을을 끊는 메소드입니다.

## 2. MyDatasource

### 데이터풀을 관리하는 클래스입니다.
```java
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
```

> - **map**에 미리 커넥션을 저장합니다. 만약 커넥션을 사용중이라면 해당 값을 `true`로 설정해줍니다.
> - **startTime**과 **time*을 이용하여 대기시간을 구현하였습니다.
> - 동시성을 방지하기 위해 `synchronized`를 붙여주었습니다.

## 3. SimpleDb

### 데이터베이스와의 연결 및 실행의 중간을 담당하는 클래스입니다.(퍼사드)
> - `퍼사드 패턴`으로 연결 및 실행을 담당하는 클래스입니다.

### SQL

![스크린샷 2023-05-16 115215](https://github.com/GreenTea9227/simpleDb/assets/95036191/dc4e2dfe-d8a2-4097-8f32-49a3177657bd)

## 4. Sql

`sql`의 `interface`입니다. 실제로 `sql`을 작성하려면 위 `interface`를 구현하여야 합니다.

## 5. SqlTransactionAbstract ,  SqlTransactionInterface

```java
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
```
>템플릿 메소드 패턴을 사용한 클래스입니다.
>위 메소드를 통해서 `commit`, `rollback` 및 **logging**과정과 **release**과정을 구현하였습니다.

## 6. SqlByAbstract,  SqlByInterface

실제 SQL을 받아서 실행하는 클래스입니다. 모든 쿼리는 해당 **SqlTransactionAbstract** 또는 **SqlTransactionInterface**를 통해 작성하였습니다.

**일부 함수**
```java
public long update() {

        SqlTransactionInterface<Long> sql = () -> Long.valueOf(executeUpdate());

        return sql.logic(this);
    }
```




