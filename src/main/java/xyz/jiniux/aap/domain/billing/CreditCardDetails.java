package xyz.jiniux.aap.domain.billing;

public record CreditCardDetails(
    String number,
    int validMonth,
    int validYear,
    String address,
    String tenant,
    int csc
) {}
