package br.com.mvgc.reactivekanban.repository;

import br.com.mvgc.reactivekanban.model.Card;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

/**
 * Repository for {@link Card} objects.
 */
public interface CardRepository extends ReactiveCassandraRepository<Card, UUID> {


    /**
     * TODO: consider using order > 0 ALLOW FILTERING in this query, since it's already issuing a query
     * against a single partition!
     *
     * also, when this is done, this may be removed: search "filter(x -> x.getCardO" (not all of them, thoug...)
     *
     * See: https://dzone.com/articles/apache-cassandra-and-allow-filtering
     *
     * @param cardListIds
     * @return
     */
    @Query("SELECT * FROM card WHERE card_list_id in ?0")
    Flux<Card> findByCardListIds(Collection<UUID> cardListIds);

    @Query("SELECT * FROM card WHERE card_list_id = ?0 AND id = ?1")
    Mono<Card> findByCardListIdAndId(UUID cardListId, UUID id);
}
