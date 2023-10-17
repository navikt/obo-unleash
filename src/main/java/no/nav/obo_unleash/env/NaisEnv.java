package no.nav.obo_unleash.env;

import java.util.Objects;

public class NaisEnv {
    private final String clusterName;

    public NaisEnv() {
        this.clusterName = System.getenv("NAIS_CLUSTER_NAME");
    }

    public boolean isLocal() {
        return Objects.equals(clusterName, "local");
    }

    public boolean isDevGCP() {
        return Objects.equals(clusterName, "dev-gcp");
    }

    public boolean isProdGCP() {
        return Objects.equals(clusterName, "prod-gcp");
    }
}