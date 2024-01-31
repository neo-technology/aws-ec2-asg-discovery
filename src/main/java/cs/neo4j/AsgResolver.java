package cs.neo4j;

import static com.neo4j.causalclustering.discovery.akka.AkkaDiscoveryServiceFactory.RESOLVER_TYPE;
import static com.neo4j.configuration.ClusterAddressSettings.discovery_advertised_address;
import static java.lang.String.format;

import com.neo4j.causalclustering.discovery.resolve.BaseRemotesResolver;
import com.neo4j.configuration.DiscoverySettings;
import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.common.DependencyResolver;
import org.neo4j.configuration.Config;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.logging.InternalLog;
import org.neo4j.logging.internal.LogService;

import java.util.stream.Stream;

@ServiceProvider
public class AsgResolver extends BaseRemotesResolver {
    public static final String NAME = "EC2-ASG";
    private String selector;
    private String awsKey;
    private String awsSecret;
    private String awsRegion;
    private int discoveryPort;
    private AwsClient awsClient;
    private InternalLog log;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected void internalInit(Config config, LogService logService, DependencyResolver externalDependencies) {
        selector = checkConfig(config, Ec2Settings.asg_name);
        awsKey = checkConfig(config, Ec2Settings.aws_key);
        awsSecret = checkConfig(config, Ec2Settings.aws_secret);
        awsRegion = checkConfig(config, Ec2Settings.aws_region);

        discoveryPort = checkConfig(config, DiscoverySettings.discovery_listen_address).getPort();

        log = logService.getUserLog(AsgResolver.class);
        log.info("Init of discovery plugin "+this.configDescription());
        awsClient = externalDependencies.containsDependency(AwsClient.class)
                ? externalDependencies.resolveDependency(AwsClient.class)
                : new AwsClient(awsKey, awsSecret, awsRegion);
    }

    @Override
    protected Stream<SocketAddress> resolveInternal() {
        try {
            return awsClient.getVmAddressesByAsgName(selector)
                    .map(s -> {
                        SocketAddress addr = new SocketAddress(s, discoveryPort);

                        return addr;
                    });
        } catch (Exception e) {
            log.error("Failed discovery "+e.getMessage());
        }
        return Stream.empty();
    }

    @Override
    protected String configDescription() {
        return format(
                "{awsRegion:'%s',ASG name:'%s'}",
                awsRegion, selector);
    }

    @Override
    public boolean resolveOnEveryJoinAttempt() {
        return true;
    }


    @Override
    public RemotesResolverType type() {
        return RESOLVER_TYPE;
    }


    @Override
    protected Setting<SocketAddress> ownAddressSetting() {
        return discovery_advertised_address;
    }
}
