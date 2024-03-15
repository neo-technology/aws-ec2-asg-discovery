package cs.neo4j;

import com.neo4j.causalclustering.discovery.resolve.RemotesResolver;
import com.neo4j.configuration.ClusterAddressSettings;
import com.neo4j.configuration.ClusterNetworkSettings;
import com.neo4j.configuration.DiscoverySettings;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.neo4j.configuration.Config;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.SettingsDeclaration;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.graphdb.config.Configuration;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.NullLogProvider;

import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.neo4j.configuration.SettingImpl.newBuilder;
import static org.neo4j.configuration.SettingValueParsers.STRING;

public class Ec2DiscoveryResolverTest {

    Ec2DiscoveryResolver resolver = new Ec2DiscoveryResolver();
    Config config = Config.newBuilder().build();

    LogProvider logProvider = NullLogProvider.getInstance();;


    @Test @Disabled
    void shouldReturnName() {
        resolver.init(RemotesResolver.Type.DISCOVERY, config, logProvider);
        assert(resolver.getName() == "EC2");
    }

    @Test @Disabled
    void shouldGetDefaultDiscoveryPort() {
        resolver.init(RemotesResolver.Type.DISCOVERY, config, logProvider);
        assert(resolver.getDiscoveryPort() == 5000);
    }

    @Test @Disabled
    void shouldGetDefaultClusterPort() {
        resolver.init(RemotesResolver.Type.CLUSTER, config, logProvider);
        assert(resolver.getDiscoveryPort() == 6000);
    }

    @Test @Disabled
    void shouldGetAdvertisedDiscoveryPort() {
        config.setIfNotSet(ClusterAddressSettings.discovery_advertised_address,
                new SocketAddress("10.0.0.2", 1234));
        resolver.init(RemotesResolver.Type.DISCOVERY, config, logProvider);
        assert(resolver.getDiscoveryPort() == 1234);
    }

    @Test @Disabled
    void shouldGetAdvertisedClusterPort() {
        config.setIfNotSet(ClusterAddressSettings.cluster_advertised_address,
                new SocketAddress("10.0.0.2", 4567));
        resolver.init(RemotesResolver.Type.CLUSTER, config, logProvider);
        assert(resolver.getDiscoveryPort() == 4567);
    }

    @Test @Disabled
    void shouldUseAsg() {
        config.setIfNotSet(Ec2DiscoverySettings.asg_name, "myasg");
        resolver.init(RemotesResolver.Type.DISCOVERY, config, logProvider);

        assert(true);
//        assert(resolver.addresses().collect(Collectors.toList()) == );
    }

    @Test @Disabled
    void shouldUseTag() {
        config.setIfNotSet(Ec2DiscoverySettings.vm_tag, "mytag:production");
        resolver.init(RemotesResolver.Type.DISCOVERY, config, logProvider);

        assert(true);
    }



}
