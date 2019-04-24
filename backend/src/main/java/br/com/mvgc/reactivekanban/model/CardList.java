package br.com.mvgc.reactivekanban.model;

import br.com.mvgc.reactivekanban.dto.CardDTO;
import br.com.mvgc.reactivekanban.utils.ListUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.*;

/**
 * Represents a list of cards in a kanban board.
 */
@Table("card_list")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardList {

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;

    @PrimaryKeyColumn(name = "board_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID boardId;

    private String title;

    @PrimaryKeyColumn(name = "list_order", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Integer listOrder;

    @Transient
    private List<Card> cards;

}
