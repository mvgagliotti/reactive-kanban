package br.com.mvgc.reactivekanban.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.QueryLogger;
import com.datastax.driver.core.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.ReactiveSession;
import org.springframework.data.cassandra.core.AsyncCassandraOperations;
import org.springframework.data.cassandra.core.AsyncCassandraTemplate;
import org.springframework.data.cassandra.core.cql.ReactiveCqlOperations;
import org.springframework.data.cassandra.core.cql.ReactiveCqlTemplate;

/**
 * Configuration class
 *
 */
@Configuration
public class Config {

    @Bean
    AsyncCassandraOperations asyncCassandraTemplate(Session session) {
        return new AsyncCassandraTemplate(session);
    }

    @Bean
    ReactiveCqlOperations reactiveCassandraOperations(ReactiveSession reactiveSession) {
        return new ReactiveCqlTemplate(reactiveSession);
    }

    @Bean
    public QueryLogger queryLogger(Cluster cluster) {
        QueryLogger queryLogger = QueryLogger.builder()
                .build();
        cluster.register(queryLogger);
        return queryLogger;
    }

}
