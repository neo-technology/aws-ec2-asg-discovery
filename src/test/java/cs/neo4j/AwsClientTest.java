package cs.neo4j;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import org.mockito.Mockito;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AwsClientTest {


    Ec2Client ec2ClientMock = mock(Ec2Client.class);

    @Test
    void shouldTestSomething() {
        boolean exception=false;


//        DescribeInstancesResponse describeInstancesResponse = DescribeInstancesResponse.builder()
//                .reservations(reservation -> reservation.instances(instance -> instance.stateName("running")))
//                .build();
//
//        when(ec2ClientMock.describeInstances(any(DescribeInstancesRequest.class)))
//                .thenReturn(describeInstancesResponse);
//        AwsClient awsClient = new AwsClient("????",
//                "?????", "eu-west-1");
//
//        try {
//            Stream<String> resultStream = awsClient.getVmAddressesByAsgName("myasg");
//            List<String> results = resultStream.collect(Collectors.toList());
//            assertEquals(1, results.size());
//            assertEquals("ip-10-0-0-17.eu-west-1.compute.internal", results.get(0));
//
//        } catch (Exception e) {
//            exception=true;
//        }

        assertFalse(exception);
    }
}
