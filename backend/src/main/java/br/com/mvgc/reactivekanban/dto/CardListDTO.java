package br.com.mvgc.reactivekanban.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * A DTO that represents a list of cards.
 */
@Data
public class CardListDTO {

    private UUID id;
    private UUID boardId;
    private String title;
    private Integer order;
    private List<CardDTO> cards;

}
