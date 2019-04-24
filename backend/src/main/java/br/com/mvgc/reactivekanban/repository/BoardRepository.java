package br.com.mvgc.reactivekanban.repository;

import br.com.mvgc.reactivekanban.model.Board;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import java.util.UUID;

/**
 * Repository for {@link Board} objects.
 *
 */
public interface BoardRepository extends ReactiveCassandraRepository<Board, UUID> {

}
