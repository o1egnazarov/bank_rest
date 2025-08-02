package ru.noleg.bankcards.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.noleg.bankcards.dto.card.CardDto;
import ru.noleg.bankcards.dto.card.CreateCardDto;
import ru.noleg.bankcards.entity.Card;

import java.util.List;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(target = "maskedNumber", expression = "java(maskCardNumber(card.getMaskedNumber()))")
    CardDto mapToCardDto(Card card);

    List<CardDto> mapToCardDtos(List<Card> cards);

    default String maskCardNumber(String cardNumber) {
        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFourDigits;
    }


    @Mapping(source = "ownerId", target = "owner.id")
    Card mapToCardEntity(CreateCardDto createCardDto);
}
