
**AWS Ec2 Auto-scaling-group Discovery plugin for Neo4j**

This neo4j plugin implements cluster discovery for a cluster deployed in an AWS EC2 Auto-scaling group. 
It retrieves the list of network addresses of the group's VMs, and feeds it to Neo4j's cluster discovery module.

**Compatilibity**

Neo4j 5.7+

Note: there could be changes to the plugin API.

**Usage**

- Put the jar in the plugins directory of every neo4j instance of the cluster
- In neo4j.conf, set the settings below


**Settings**

- `dbms.cluster.discovery.resolver_type=EC2-ASG`
- `server.config.strict_validation.enabled=false` : to disable strict settings validation, which will allow the usage of the following  plugin-specific settings (You'll still get Warnings : "Unrecognized setting").

- `dbms.aws.asg_name=<asg_name>` : the name of the Auto-scaling group
- `dbms.aws.region=<region>`     : the AWS region hosting the Auto-scaling group (ex: "eu-west-1")
- `dbms.aws.key=<key>`           : the Access Key of the user connecting to the AWS API.
- `dbms.aws.secret=<secret>`     : the Secret Key of the user connecting to the AWS API

**Permissions**

The AWS User requires the following permissions :
- "ec2:DescribeInstances",
- "autoscaling:DescribeAutoScalingGroups"

