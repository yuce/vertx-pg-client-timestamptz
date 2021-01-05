package example;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.Tuple;

public class Main {
    static PgPool client;

    static String CREATE_QUERY =
            "DROP TABLE IF EXISTS table1;\n" +
                    "CREATE TABLE table1 (\n" +
                    "    item_id BIGSERIAL PRIMARY KEY,\n" +
                    "    created_at TIMESTAMPTZ\n" +
                    ")";

    // THIS WORKS with vertx-pg-client 4.0.0
    //    static String INSERT_QUERY =
    //            "INSERT INTO table1 (created_at)\n" +
    //            "VALUES (TO_DATE($1, 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"'))";

    // THIS WORKS with PostgreSQL 12
    //      WORKS with JDBC
    //      WORKS with vertx-jdbc-client 4.0.0
    //      DOESN'T WORK with vertx-pg-client 4.0.0
    static String INSERT_QUERY =
            "INSERT INTO table1 (created_at)\n" +
                    "VALUES ($1::timestamptz)";

    static Object[] PARAMS = new Object[]{"2017-12-03T10:15:30Z"};

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(5432)
                .setHost("localhost")
                .setUser("postgres")
                .setPassword("123")
                .setDatabase("postgres");
        client = PgPool.pool(vertx, connectOptions, new PoolOptions());
        ensureTableExists()
                .compose(Main::prepareQuery)
                .compose(preparedStmt ->
                        preparedStmt.query().execute(Tuple.from(PARAMS))
                ).onComplete(it -> {
            if (it.succeeded()) System.out.println("OK");
            else System.out.println("Failed: " + it.cause());
            vertx.close();
        });
    }

    static Future<PreparedStatement> prepareQuery(Void unused) {
        return client.getConnection().compose(conn -> conn.prepare(INSERT_QUERY));
    }

    static Future<Void> ensureTableExists() {
        return client.query(CREATE_QUERY).execute().mapEmpty();
    }
}
