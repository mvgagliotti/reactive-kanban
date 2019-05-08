package br.com.mvgc.reactivekanban.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardMoveDTO {

    private UUID fromList;
    private UUID toList;
    private UUID card;
    private UUID cardAtTargetPosition;

}
