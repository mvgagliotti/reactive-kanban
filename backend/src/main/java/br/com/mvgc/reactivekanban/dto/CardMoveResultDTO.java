package br.com.mvgc.reactivekanban.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CardMoveResultDTO {

    private UUID sourceListId;
    private UUID targetListId;
    private List<CardDTO> sourceListCards;
    private List<CardDTO> targetListCards;

}
