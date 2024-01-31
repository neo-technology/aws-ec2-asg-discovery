
**AWS Ec2 Auto-scaling-group Discovery plugin for Neo4j**

This neo4j plugin implements cluster discovery for a cluster deployed in an AWS EC2 Auto-scaling group. 
It retrieves the list of network addresses of the group's VMs, and feeds it to Neo4j's cluster discovery module.

Notes:
- It retrieves the VMs' private DnsName or IpAddress.
- It retrieves the discovery TCP port from the neo4j setting "server.discovery.listen_address"


**Compatilibity**

Neo4j 5.7+

Note: there could be changes to the plugin API.

**Usage**

- Put the jar in the plugins directory of every neo4j instance of the cluster
- In neo4j.conf, set the settings below


**Settings**

- `dbms.cluster.discovery.resolver_type=EC2-ASG`   : select the discovery protocol implemented by this plugin
- `server.config.strict_validation.enabled=false`  : to disable strict settings validation, which will allow the usage of the following plugin-specific settings (You may still get Warnings : "Unrecognized setting").
- `dbms.cluster.discovery.aws.asg_name=<asg_name>` : the name of the Auto-scaling group
- `dbms.cluster.discovery.aws.region=<region>`     : the AWS region hosting the Auto-scaling group (ex: "eu-west-1")

Optionally :
- `dbms.cluster.discovery.aws.key=<key>`           : the Access Key of the user connecting to the AWS API.
- `dbms.cluster.discovery.aws.secret=<secret>`     : the Secret Key of the user connecting to the AWS API.
- `dbms.cluster.discovery.aws.address_type=<type>` : type of network address to retrieve from the VM, to use for discovery. One of PRIVATE_IP|PRIVATE_DNSNAME|PUBLIC_IP|PUBLIC_DNSNAME. Defaults to PRIVATE_IP. Must match the type of `server.discovery.advertised_address`.


If not set, the plugin will try to use any InstanceProfile role attached to the EC2 instance. That can be defined in the ASG's LaunchTemplate.

**Permissions**

- The Role/User requires the following permissions :
  - "ec2:DescribeInstances",
  - "autoscaling:DescribeAutoScalingGroups"

- The auto-scaling group VMs require a Security Group that allows traffic on TCP ports 5000, 6000, 7000, 7688 (for internal cluster communication) as well as TCP ports 7474 and 7687 (for external access).