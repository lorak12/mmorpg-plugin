package org.nakii.mmorpg.economy;

import java.time.Instant;

/**
 * A record to hold the details of a single bank transaction.
 * Storing the timestamp allows for "X time ago" calculations.
 */
public record Transaction(TransactionType type, double amount, long timestamp) {
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, INTEREST
    }
}