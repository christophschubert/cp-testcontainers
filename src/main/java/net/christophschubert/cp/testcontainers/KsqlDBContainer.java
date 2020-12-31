package net.christophschubert.cp.testcontainers;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class KsqlDBContainer extends CPTestContainer<KsqlDBContainer> {

    static final int defaultPort = 8088;

    KsqlDBContainer(DockerImageName dockerImageName, KafkaContainer bootstrap, Network network) {
        super(dockerImageName, bootstrap, network, defaultPort);

        withEnv("SCHEMA_REGISTRY_HOST_NAME", "ksqldb-server");
        withEnv("KSQL_BOOTSTRAP_SERVERS", getInternalBootstrap(bootstrap));
        withEnv("KSQL_LISTENERS", getHttpPortListener());
        withEnv("KSQL_CACHE_MAX_BYTES_BUFFERING", "0");
        waitingFor(Wait.forHttp("/"));
    }

    /**
     * Sets the service Id of the ksqlDB server. The service Id is used as prefix to the internal topics.
     *
     * @param serviceId the service Id.
     * @return this container
     */
    public KsqlDBContainer withServiceId(String serviceId) {
        withEnv("KSQL_KSQL_SERVICE_ID", serviceId);
        return this;
    }

    public KsqlDBContainer withQueriesFile(String queriesFile) {
        final String containerPath = "/queries.sql";
        withCopyFileToContainer(MountableFile.forHostPath(queriesFile), containerPath);
        withEnv("KSQL_KSQL_QUERIES_FILE", containerPath);
        return this;
    }

    public KsqlDBContainer withSchemaRegistry(SchemaRegistryContainer schemaRegistry) {
        withEnv("KSQL_KSQL_SCHEMA_REGISTRY_URL", schemaRegistry.getInternalBaseUrl());
        dependsOn(schemaRegistry);
        return this;
    }
}