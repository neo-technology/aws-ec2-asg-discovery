package cs.neo4j;

import org.neo4j.kernel.lifecycle.LifecycleAdapter;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.neo4j.logging.InternalLog;
import org.neo4j.logging.internal.LogService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.imds.Ec2MetadataClient;
import software.amazon.awssdk.imds.Ec2MetadataResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;

public class AwsClient extends LifecycleAdapter {

    private static String accessKey;
    private static String secretKey;
    private static String region;
    private static Ec2Settings.AddressType addressType;

    private InternalLog log;
    private AutoScalingClient autoScalingClient;
    private Ec2Client ec2Client;

    public AwsClient(String region, Ec2Settings.AddressType addressType, InternalLog log) {
        this.region=region;
        this.addressType=addressType;
        this.log=log;
        createClients();
    }

    public AwsClient(String accessKey, String secretKey, String region, Ec2Settings.AddressType addressType, InternalLog log) {
        this.accessKey=accessKey;
        this.secretKey=secretKey;
        this.region=region;
        this.addressType=addressType;
        this.log=log;

        createClients();
    }

    private void createClients(){
        if (region != null) {
            this.autoScalingClient = AutoScalingClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(awsCredentialsProvider())
                    .build();

            this.ec2Client = Ec2Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(awsCredentialsProvider())
                    .build();
        } else {
            this.autoScalingClient = AutoScalingClient.builder()
                    .credentialsProvider(awsCredentialsProvider())
                    .build();

            this.ec2Client = Ec2Client.builder()
                    .credentialsProvider(awsCredentialsProvider())
                    .build();
            Ec2MetadataClient imdsClient = Ec2MetadataClient.builder()
                    .build();
            Ec2MetadataResponse metadataResponse = imdsClient.get("/latest/meta-data/");
            log.info(metadataResponse.asString());
        }

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
//        System.out.println("Group Name: " + group.autoScalingGroupName());
//        System.out.println("Group ARN: " + group.autoScalingGroupARN());
        return group;
    }

    public Stream<String> getVmAddressesByAsgName(String nameSelector) throws Exception {

        AutoScalingGroup group = getAsgByName(nameSelector);

//        for (Instance i: group.instances()) {
//            System.out.println("Instance: " + i.instanceId());
//            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
//                    .instanceIds(i.instanceId())
//                    .build();
//            DescribeInstancesResponse ec2Response = ec2Client.describeInstances(request);
//            for (Reservation reservation : ec2Response.reservations()) {
//                for (software.amazon.awssdk.services.ec2.model.Instance instance : reservation.instances()) {
//                    if (instance.state().nameAsString().equals("running")) {
//                        System.out.println("  Instance Id is " + instance.instanceId());
//                        System.out.println("  privateDnsName is " + instance.privateDnsName());
//                        System.out.println("  privateIpAddress is " + instance.privateIpAddress());
//                        System.out.println("  publicDnsName is " + instance.publicDnsName());
//                        System.out.println("  publicIpAddress is " + instance.publicIpAddress());
//                        System.out.println("  state is " + instance.state().nameAsString());
//                    }
//                }
//            }
//        }
        return group.instances().stream()
                .map( i -> { //map from autoscaling Instance to Ec2 Instance
                                DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                                        .instanceIds(i.instanceId())
                                        .build();
                                DescribeInstancesResponse ec2Response = ec2Client.describeInstances(request);
                                return ec2Response.reservations().get(0).instances().get(0);

                })
                .filter (i -> i.state().nameAsString().equals("running"))
                .map( i -> {
                                if (addressType.equals(Ec2Settings.AddressType.PRIVATE_DNSNAME) &&
                                        i.privateDnsName() != null) {
                                    return i.privateDnsName();
                                }
                                if (addressType.equals(Ec2Settings.AddressType.PRIVATE_IP) &&
                                            i.privateIpAddress() != null) {
                                    return i.privateIpAddress();
                                }
                                if (addressType.equals(Ec2Settings.AddressType.PUBLIC_DNSNAME) &&
                                        i.publicDnsName() != null) {
                                    return i.publicDnsName();
                                }
                                if (addressType.equals(Ec2Settings.AddressType.PUBLIC_IP) &&
                                        i.publicIpAddress() != null) {
                                    return i.publicIpAddress();
                                }
                                return "";
                            });

    }


}
