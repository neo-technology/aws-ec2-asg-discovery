
**AWS EC2 Discovery plugin for Neo4j**

This Neo4j plugin provides a list of seed addresses to the discovery process by querying the AWS API for a list of EC2 instances matching certain criteria determined by the plugin settings.

The EC2 instances can be found by :
- the name of the AutoScaling Group they belong to
- a Tag

Notes:
- VMs must be running
- the retrieved network address can be one of: private DnsName, public DnsName, private IpAddress or public IpAddress.
- the discovery TCP port is extracted from the neo4j setting "server.discovery.listen_address" (or the default port is used otherwise)


**Compatilibity**

Neo4j 5.18+


**Installation**

- Put the jar in the plugins directory of every neo4j instance of the cluster
- In neo4j.conf, set the settings below


**Settings**

- `dbms.cluster.discovery.resolver_type=EC2`   : enable the plugin
- `server.config.strict_validation.enabled=false`  : disable strict settings validation, which will allow the usage of the following plugin-specific settings (You may still get Warnings : "Unrecognized setting").

One of the following to specify how to find the VMs:
- `dbms.cluster.discovery.aws.asg_name=<asg_name>` : the name of the Auto-scaling group which contains the VMs. Takes precedence over "vm_tag".
- `dbms.cluster.discovery.aws.vm_tag`              : VM tag in the format "tagKey:value" (ex: `dbms.cluster.discovery.aws.vm_tag=cluster:neo4jprod`). Ignored if "asg_name" is set.

Optionally :
- `dbms.cluster.discovery.aws.region=<region>`     : the AWS region hosting the Auto-scaling group (ex: "eu-west-1"). If not set, the plugin will attempt to retrieve the region from the VM metadata

- `dbms.cluster.discovery.aws.key=<key>`           : the Access Key of the user connecting to the AWS API.
- `dbms.cluster.discovery.aws.secret=<secret>`     : the Secret Key of the user connecting to the AWS API.

  If key/secret are not set, the plugin will try to use any InstanceProfile role attached to the EC2 instance. See below for required permissions. That can be defined in the ASG's LaunchTemplate. 
  These settings are sensitive and should be stored safely.

- `dbms.cluster.discovery.aws.address_type=<type>` : type of network address to retrieve from the VM, to use for discovery. One of PRIVATE_IP|PRIVATE_DNSNAME|PUBLIC_IP|PUBLIC_DNSNAME. Defaults to PRIVATE_IP. Must match the type of `server.discovery.advertised_address`.



**Permissions**

- The Role/User requires the following permissions :
  - "ec2:DescribeInstances",
  - "autoscaling:DescribeAutoScalingGroups"
