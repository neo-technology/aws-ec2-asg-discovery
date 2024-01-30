package cs.neo4j;

import org.neo4j.annotations.api.PublicApi;
import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.configuration.Description;
import org.neo4j.configuration.SettingsDeclaration;
import org.neo4j.graphdb.config.Setting;


import static org.neo4j.configuration.SettingImpl.newBuilder;
import static org.neo4j.configuration.SettingValueParsers.STRING;


@Description("Settings for Kubernetes")
@ServiceProvider
@PublicApi
public class Ec2Settings implements SettingsDeclaration {

    @Description("Auto-scaling group name")
    public static final Setting<String> asg_name = newBuilder(
            "dbms.aws.asg_name", STRING, null)
            .build();

    @Description("AWS access key")
    public static final Setting<String> aws_key = newBuilder(
            "dbms.aws.key", STRING, null)
            .build();

    @Description("AWS secret")
    public static final Setting<String> aws_secret = newBuilder(
            "dbms.aws.secret", STRING, null)
            .build();


    @Description("AWS region")
    public static final Setting<String> aws_region = newBuilder(
            "dbms.aws.region", STRING, null)
            .build();
}
