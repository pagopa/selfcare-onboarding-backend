package it.pagopa.selfcare.onboarding.connector.model;

public enum Certification {
    NONE, SPID;

    public static boolean isCertified(Certification certification) {
        return certification != null && !Certification.NONE.equals(certification);
    }

}
