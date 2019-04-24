package br.com.mvgc.reactivekanban.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * A DTO for boards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {

    private UUID id;
    private String description;
    private List<CardListDTO> cardLists;

}
