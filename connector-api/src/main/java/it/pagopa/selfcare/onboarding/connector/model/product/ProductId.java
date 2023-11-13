package it.pagopa.selfcare.onboarding.connector.model.product;

public enum ProductId {

    PROD_INTEROP("prod-interop"),
    PROD_IO("prod-io"),
    PROD_PAGOPA("prod-pagopa"),
    PROD_FD("prod-fd"),
    PROD_FD_GARANTITO("prod-fd-garantito"),
    PROD_PN_PG("prod-pn-pg");

    private final String value;

    ProductId(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
