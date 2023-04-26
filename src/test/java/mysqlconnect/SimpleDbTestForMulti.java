package mysqlconnect;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public class SimpleDbTestForMulti {

    private SimpleDb simpleDb;

    @BeforeAll
    public void beforeAll() {

        simpleDb = new SimpleDb("localhost", "yohan", "1111", "simpleDb__test");
        simpleDb.setDevMode(true);

        createArticleTable();
    }

    @BeforeEach
    public void beforeEach() {

        simpleDb.clear();
        simpleDb = new SimpleDb("localhost", "yohan", "1111", "simpleDb__test");
        simpleDb.setDevMode(true);
        truncateArticleTable();
        makeArticleTestData();

    }

    private void makeArticleTestData() {
        IntStream.rangeClosed(1, 6).forEach(no -> {
            boolean isBlind = no > 3;
            String title = "제목%d".formatted(no);
            String body = "내용%d".formatted(no);

            simpleDb.run("""
                    INSERT INTO article
                    SET createdDate = NOW(),
                    modifiedDate = NOW(),
                    title = ?,
                    `body` = ?,
                    isBlind = ?
                    """, title, body, isBlind);
        });
    }

    private void createArticleTable() {
        simpleDb.run("DROP TABLE IF EXISTS article");

        simpleDb.run("""                                                
                CREATE TABLE article (
                    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                    PRIMARY KEY(id),
                    createdDate DATETIME NOT NULL,
                    modifiedDate DATETIME NOT NULL,
                    title VARCHAR(100) NOT NULL,
                    `body` TEXT NOT NULL,
                    isBlind BIT(1) NOT NULL DEFAULT(0)
                )
                """);
    }

    private void truncateArticleTable() {
        simpleDb.run("TRUNCATE article");
    }

    @Test
    public void failToExceedThreadSize() {
        Assertions.assertThatThrownBy(() -> {
            for (int i = 0; i < 6; i++) {
                Sql sql = simpleDb.genSql();
            }
        }).isInstanceOf(NoConnection.class);

    }
}
