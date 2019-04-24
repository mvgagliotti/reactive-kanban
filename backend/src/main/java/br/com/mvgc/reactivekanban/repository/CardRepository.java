package br.com.mvgc.reactivekanban.repository;

import br.com.mvgc.reactivekanban.model.Card;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.UUID;

/**
 * Repository for {@link Card} objects.
 */
public interface CardRepository extends ReactiveCassandraRepository<Card, UUID> {

    @Query("SELECT * FROM card WHERE card_list_id in ?0")
    Flux<Card> findByCardListIds(Collection<UUID> cardListIds);
}
