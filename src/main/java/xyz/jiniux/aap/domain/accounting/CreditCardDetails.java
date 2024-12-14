package xyz.jiniux.aap.domain.accounting;

public record CreditCardDetails(
    String number,
    int validMonth,
    int validYear,
    String address,
    String tenant,
    int csc
) {}
