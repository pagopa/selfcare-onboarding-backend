package it.pagopa.selfcare.onboarding.connector.model.onboarding;

import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import lombok.Data;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Data
public class UserInfo {

    private String id;
    private String name;
    private String surname;
    private String taxCode;
    private String institutionId;
    private String email;
    private PartyRole role;
    private String status;
    private boolean certified;

    @Data
    public static class UserInfoFilter {
        private Optional<PartyRole> role = Optional.empty();
        private Optional<String> productId = Optional.empty();
        private Optional<Set<String>> productRoles = Optional.empty();
        private Optional<String> userId = Optional.empty();
        private Optional<EnumSet<RelationshipState>> allowedStates = Optional.empty();

        public void setRole(Optional<PartyRole> role) {
            this.role = role == null ? Optional.empty() : role;
        }

        public void setProductId(Optional<String> productId) {
            this.productId = productId == null ? Optional.empty() : productId;
        }

        public void setProductRoles(Optional<Set<String>> productRoles) {
            this.productRoles = productRoles == null ? Optional.empty() : productRoles;
        }

        public void setUserId(Optional<String> userId) {
            this.userId = userId == null ? Optional.empty() : userId;
        }

        public void setAllowedState(Optional<EnumSet<RelationshipState>> allowedStates) {
            this.allowedStates = allowedStates == null ? Optional.empty() : allowedStates;
        }
    }
}
