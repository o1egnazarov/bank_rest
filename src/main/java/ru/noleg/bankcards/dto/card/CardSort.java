package ru.noleg.bankcards.dto.card;

import org.springframework.data.domain.Sort;

public enum CardSort {

    ID_ASC(Sort.by(Sort.Direction.ASC, "id")),
    ID_DESC(Sort.by(Sort.Direction.DESC, "id")),
    DATE_ASC(Sort.by(Sort.Direction.ASC, "expirationDate"));

    private final Sort sortValue;

    CardSort(Sort sortValue) {
        this.sortValue = sortValue;
    }

    public Sort getSortValue() {
        return sortValue;
    }
}