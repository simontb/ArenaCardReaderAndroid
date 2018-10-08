package de.simontb.arenacardreader;

public class CardBalanceModel {

    public enum CardStatus {
        VALID, INVALID
    }

    private CardStatus status;
    private long lastBalance;

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public long getLastBalance() {
        return lastBalance;
    }

    public void setLastBalance(Integer lastBalance) {
        this.lastBalance = lastBalance;
    }

}
