package ru.noleg.bankcards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import ru.noleg.bankcards.jpa.converter.YearMonthAttributeConverter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

@Entity
@Table(name = "t_cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_id")
    private Long id;

    @Column(name = "c_number", nullable = false, unique = true)
    private String encryptedNumber;

    @Transient
    private String maskedNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_owner_id")
    private User owner;

    @Column(name = "c_expiration_date", nullable = false)
    @Convert(converter = YearMonthAttributeConverter.class)
    private YearMonth expirationDate;

    @Column(name = "c_card_status")
    @Enumerated(value = EnumType.STRING)
    private CardStatus status;

    @Column(name = "c_balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    public Card() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEncryptedNumber() {
        return encryptedNumber;
    }

    public void setEncryptedNumber(String number) {
        this.encryptedNumber = number;
    }

    public String getMaskedNumber() {
        return maskedNumber;
    }

    public void setMaskedNumber(String maskedNumber) {
        this.maskedNumber = maskedNumber;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public YearMonth getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(YearMonth expirationDate) {
        this.expirationDate = expirationDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Card card = (Card) object;
        return Objects.equals(id, card.id) &&
                Objects.equals(encryptedNumber, card.encryptedNumber) &&
                Objects.equals(maskedNumber, card.maskedNumber) &&
                Objects.equals(owner, card.owner) &&
                Objects.equals(expirationDate, card.expirationDate) &&
                status == card.status &&
                Objects.equals(balance, card.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, encryptedNumber, maskedNumber, owner, expirationDate, status, balance);
    }
}
