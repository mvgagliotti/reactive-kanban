package br.com.mvgc.reactivekanban.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * A DTO that represents a card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO implements Comparable<CardDTO> {

    private UUID id;
    private UUID cardListId;
    private UUID boardId;
    private String title;
    private Integer order=0;
    private String description;

    @Override
    public int compareTo(CardDTO o) {
        return this.getOrder().compareTo(o.getOrder());
    }
}
