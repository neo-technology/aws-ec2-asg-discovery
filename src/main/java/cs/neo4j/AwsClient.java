package cs.neo4j;

import org.neo4j.kernel.lifecycle.LifecycleAdapter;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.neo4j.logging.Log;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.AutoScalingClientBuilder;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.Ec2ClientBuilder;
import software.amazon.awssdk.services.ec2.model.*;


public class AwsClient extends LifecycleAdapter {

    private static String accessKey;
    private static String secretKey;
    private static String region;
    private static Ec2DiscoverySettings.AddressType addressType;

    private Log log;
    private AutoScalingClient autoScalingClient;
    private Ec2Client ec2Client;

    public AwsClient(String region, Ec2DiscoverySettings.AddressType addressType, Log log) {
        this.region=region;
        this.addressType=addressType;
        this.log=log;

        createClients();
    }

    public AwsClient(String accessKey, String secretKey, String region, Ec2DiscoverySettings.AddressType addressType, Log log) {
        this.accessKey=accessKey;
        this.secretKey=secretKey;
        this.region=region;
        this.addressType=addressType;
        this.log=log;

        createClients();
    }

    private void createClients(){
        AutoScalingClientBuilder autoScalingClientBuilder = AutoScalingClient.builder()
                .credentialsProvider(awsCredentialsProvider());
        Ec2ClientBuilder ec2ClientBuilder = Ec2Client.builder()
                .credentialsProvider(awsCredentialsProvider());
        if (region != null) {
            autoScalingClientBuilder.region(Region.of(region));
            ec2ClientBuilder.region(Region.of(region));
        }
        this.autoScalingClient = autoScalingClientBuilder.build();
        this.ec2Client = ec2ClientBuilder.build();

    }

    private AwsCredentialsProvider awsCredentialsProvider() {
        if (accessKey != null && secretKey != null) {
            return () -> AwsBasicCredentials.create(accessKey, secretKey);
        } else {
            return InstanceProfileCredentialsProvider.builder().build();
        }
    }

    private AutoScalingGroup getAsgByName(String nameSelector) {
        DescribeAutoScalingGroupsResponse response = autoScalingClient.describeAutoScalingGroups();

        AutoScalingGroup group = response.autoScalingGroups().stream()
                .filter(g->g.autoScalingGroupName().equals(nameSelector))
                .limit(1)
                .collect(Collectors.toList()).get(0);
        return group;
    }

    public Stream<String> getVmAddressesByAsgName(String nameSelector) throws Exception {

        AutoScalingGroup group = getAsgByName(nameSelector);

        return group.instances().stream()
                .map( i -> { //map from autoscaling Instance to Ec2 Instance
                                DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                                        .instanceIds(i.instanceId())
                                        .build();
                                DescribeInstancesResponse ec2Response = ec2Client.describeInstances(request);
                                return ec2Response.reservations().get(0).instances().get(0);

                })
                .filter (i -> i.state().nameAsString().equals("running"))
                .map( i -> getInstanceAddress(i));
    }

    public Stream<String> getVmAddressesByTag(String tagKey, String tagValue) throws Exception {
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .build();
        DescribeInstancesResponse ec2Response = ec2Client.describeInstances(request);

        return ec2Response.reservations().stream().flatMap(r -> r.instances().stream())
                .filter(i -> i.state().nameAsString().equals("running"))
                .filter(i -> matchTags(i, tagKey, tagValue))
                .map( i -> getInstanceAddress(i));
    }

    private boolean matchTags (Instance instance, String tagKey, String tagValue) {
        for (Tag t : instance.tags()) {
            if (tagKey.equals(t.key()) && tagValue.equals(t.value())) {
                return true;
            }
        }
        return false;
    }

    private String getInstanceAddress(Instance i) {
        if (addressType.equals(Ec2DiscoverySettings.AddressType.PRIVATE_DNSNAME) &&
                i.privateDnsName() != null) {
            return i.privateDnsName();
        }
        if (addressType.equals(Ec2DiscoverySettings.AddressType.PRIVATE_IP) &&
                i.privateIpAddress() != null) {
            return i.privateIpAddress();
        }
        if (addressType.equals(Ec2DiscoverySettings.AddressType.PUBLIC_DNSNAME) &&
                i.publicDnsName() != null) {
            return i.publicDnsName();
        }
        if (addressType.equals(Ec2DiscoverySettings.AddressType.PUBLIC_IP) &&
                i.publicIpAddress() != null) {
            return i.publicIpAddress();
        }
        return "";
    }

}
