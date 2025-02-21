package xyz.jiniux.aap.domain.billing;

public record CreditCardDetails(
    String number,
    int validMonth,
    int validYear,
    String tenant,
    int csc
) {
    public String getHiddenNumber() {
        return "****-****-****-" + number.substring(number.length() - 4);
    }
}
