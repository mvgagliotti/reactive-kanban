package br.com.mvgc.reactivekanban.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

/**
 * A kanban board
 *
 */
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @PrimaryKey
    private UUID id; //map UUID?

    private String description;

    @Transient
    private List<CardList> cardLists;

}
