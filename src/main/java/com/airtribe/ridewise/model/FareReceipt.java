package com.airtribe.ridewise.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FareReceipt {

    private final String rideId;
    private final double amount;
    private final LocalDateTime generatedAt;

    public FareReceipt(String rideId, double amount) {
        this.rideId = rideId;
        this.amount = amount;
        this.generatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("FareReceipt{rideId='%s', amount=₹%.2f, generatedAt=%s}",
                rideId, amount, generatedAt);
    }
}
