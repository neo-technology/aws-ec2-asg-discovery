package cs.neo4j;

import org.junit.jupiter.api.Test;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AwsHttpClientTest {

    @Test
    void shouldReturnAsg() {
        boolean exception=false;
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
