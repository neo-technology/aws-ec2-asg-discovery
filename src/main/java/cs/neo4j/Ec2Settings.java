package cs.neo4j;

import org.neo4j.annotations.api.PublicApi;
import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.configuration.Description;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.SettingsDeclaration;
import org.neo4j.graphdb.config.Setting;


import static org.neo4j.configuration.SettingImpl.newBuilder;
import static org.neo4j.configuration.SettingValueParsers.STRING;
import static org.neo4j.configuration.SettingValueParsers.ofEnum;


@Description("Settings for Kubernetes")
@ServiceProvider
@PublicApi
public class Ec2Settings implements SettingsDeclaration {
    public enum AddressType {
        PRIVATE_IP,
        PRIVATE_DNSNAME,
        PUBLIC_IP,
        PUBLIC_DNSNAME
    }


    @Description("Auto-scaling group name")
    public static final Setting<String> asg_name = newBuilder(
            "dbms.cluster.discovery.aws.asg_name", STRING, null)
            .build();

    @Description("AWS access key")
    public static final Setting<String> aws_key = newBuilder(
            "dbms.cluster.discovery.aws.key", STRING, null)
            .build();

    @Description("AWS secret")
    public static final Setting<String> aws_secret = newBuilder(
            "dbms.cluster.discovery.aws.secret", STRING, null)
            .build();

    @Description("AWS region")
    public static final Setting<String> aws_region = newBuilder(
            "dbms.cluster.discovery.aws.region", STRING, null)
            .build();

    @Description("Type of network address to retrieve from the VM, to use for discovery. One of [PRIVATE_IP|PRIVATE_DNSNAME|PUBLIC_IP|PUBLIC_DNSNAME]. Must match type of server.discovery.advertised_address.")
    public static final Setting<AddressType> aws_address_type = newBuilder(
            "dbms.cluster.discovery.aws.address_type", ofEnum(AddressType.class), AddressType.PRIVATE_IP)
            .build();
}
