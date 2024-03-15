package cs.neo4j;

import static com.neo4j.configuration.ClusterBaseSettings.DEFAULT_DISCOVERY_PORT;
import static com.neo4j.configuration.ClusterBaseSettings.DEFAULT_TRANSACTION_PORT;
import com.neo4j.causalclustering.discovery.resolve.RemotesResolver;
import com.neo4j.configuration.ClusterAddressSettings;
import com.neo4j.configuration.DiscoverySettings;
import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.graphdb.config.Configuration;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;

import java.util.stream.Stream;

@ServiceProvider
public class Ec2DiscoveryResolver implements RemotesResolver {
    private static final String NAME = "EC2";
    private Type type;
    private Log log;
    private Configuration configuration;


    //settings
    private String asgName;
    private String vmTag;
    private String vmTagKey;
    private String vmTagValue;
    private String awsKey;
    private String awsSecret;
    private String awsRegion;
    private Ec2DiscoverySettings.AddressType addressType;
    private int discoveryPort;
    private AwsClient awsClient;

    private static String tagSeparator = ":";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init(Type type, Configuration configuration, LogProvider logProvider) {
        this.type = type;
        this.log = logProvider.getLog(getClass());
        this.configuration = configuration;
        log.info("Init of discovery plugin "+this.NAME);

        this.asgName = configuration.get(Ec2DiscoverySettings.asg_name);
        this.awsRegion = configuration.get(Ec2DiscoverySettings.aws_region);
        this.awsKey = configuration.get(Ec2DiscoverySettings.aws_key);
        this.awsSecret = configuration.get(Ec2DiscoverySettings.aws_secret);
        this.addressType = configuration.get(Ec2DiscoverySettings.aws_address_type);
        this.vmTag = configuration.get(Ec2DiscoverySettings.vm_tag);
        if (this.vmTag != null) {
            this.vmTagKey = this.vmTag.split(tagSeparator)[0];
            this.vmTagValue = this.vmTag.split(tagSeparator)[1];
        }

        discoveryPort = getDiscoveryPort();
        awsClient = instantiateAwsClient();
    }

    public int getDiscoveryPort() {
        int port=0;
        port = switch (this.type) {
            case DISCOVERY -> configuration.get(ClusterAddressSettings.discovery_advertised_address).getPort();
            case CLUSTER -> configuration.get(ClusterAddressSettings.cluster_advertised_address).getPort();
        };
        if (port == 0) {
            port = switch (this.type) {
                case DISCOVERY -> DEFAULT_DISCOVERY_PORT;
                case CLUSTER -> DEFAULT_TRANSACTION_PORT;
            };
        }
        return port;
    }

    private AwsClient instantiateAwsClient() {
        if (this.awsKey != null  && this.awsSecret != null) {
            return new AwsClient(this.awsKey, this.awsSecret, this.awsRegion, this.addressType, this.log);
        } else {
            return new AwsClient(this.awsRegion, this.addressType, this.log);
        }
    }


    @Override
    public Stream<SocketAddress> addresses() {
        //TODO: may need to re-instantiate the aws client?
        try {
            //ASG name priority vs VM tag
            if (this.asgName != null) {
                return awsClient.getVmAddressesByAsgName(this.asgName)
                        .map(s -> {
                            SocketAddress addr = new SocketAddress(s, discoveryPort);
                            return addr;
                        });
            } else if (this.vmTag != null) {
                return awsClient.getVmAddressesByTag(this.vmTagKey, this.vmTagValue)
                        .map(s -> {
                            SocketAddress addr = new SocketAddress(s, discoveryPort);
                            return addr;
                        });
            }
        } catch (Exception e) {
            log.error("Failed discovery "+e.getMessage());
        }
        return Stream.empty();
    }

}
