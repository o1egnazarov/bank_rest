package ru.noleg.bankcards.dto.user;

import org.springframework.data.domain.Sort;

public enum UserSort {

    ID_ASC(Sort.by(Sort.Direction.ASC, "id")),
    ID_DESC(Sort.by(Sort.Direction.DESC, "id")),
    LAST_NAME_ASC(Sort.by(Sort.Direction.ASC, "lastName"));

    private final Sort sortValue;

    UserSort(Sort sortValue) {
        this.sortValue = sortValue;
    }

    public Sort getSortValue() {
        return sortValue;
    }
}
