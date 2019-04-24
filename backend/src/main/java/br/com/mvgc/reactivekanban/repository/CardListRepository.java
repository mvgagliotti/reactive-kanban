package br.com.mvgc.reactivekanban.repository;

import br.com.mvgc.reactivekanban.model.CardList;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Repository for {@link CardList} objects.
 */
public interface CardListRepository extends ReactiveCassandraRepository<CardList, UUID> {

    Flux<CardList> findByBoardId(UUID id);
}
