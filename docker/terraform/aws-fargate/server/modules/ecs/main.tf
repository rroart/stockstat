data "aws_vpc" "vpc" {
  #default = true
  filter {
    name = "tag:Name"
    values = ["${var.environment}-vpc"]
  }
}

/*====
ECS cluster
======*/
data "aws_ecs_cluster" "cluster" {
  cluster_name = "${var.environment}-ecs-cluster"
}

/*
* IAM service role
*/
data "aws_iam_policy_document" "ecs_service_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs.amazonaws.com"]
    }
  }
}

data "aws_iam_role" "ecs_role" {
  name               = "ecs_role"
}

data "aws_iam_policy_document" "ecs_service_policy" {
  statement {
    effect    = "Allow"
    resources = ["*"]
    actions = [
      "elasticloadbalancing:Describe*",
      "elasticloadbalancing:DeregisterInstancesFromLoadBalancer",
      "elasticloadbalancing:RegisterInstancesWithLoadBalancer",
      "ec2:Describe*",
      "ec2:AuthorizeSecurityGroupIngress",
    ]
  }
}

/* ecs service scheduler role */
resource "aws_iam_role_policy" "ecs_service_role_policy" {
  name = "ecs_service_role_policy"
  #policy = "${file("${path.module}/policies/ecs-service-role.json")}"
  policy = data.aws_iam_policy_document.ecs_service_policy.json
  role   = data.aws_iam_role.ecs_role.id
}

/* role that the Amazon ECS container agent and the Docker daemon can assume */
data "aws_iam_role" "ecs_execution_role" {
  name               = "ecs_task_execution_role"
}

resource "aws_iam_role_policy" "ecs_execution_role_policy" {
  name   = "ecs_execution_role_policy"
  policy = file("${path.module}/../../../common/modules/ecs/policies/ecs-execution-role-policy.json")
  role   = data.aws_iam_role.ecs_execution_role.id
}

/*====
ECS service
======*/

/* Security Group for ECS */
data "aws_security_group" "ecs_service" {
  #vpc_id      = var.vpc_id
  name        = "${var.environment}-ecs-service-sg"
  filter {
    name = "tag:Name"
    values = ["${var.environment}-ecs-service-sg"]
  }
}

data "aws_iam_role" "ecs_autoscale_role" {
  name               = "${var.environment}_ecs_autoscale_role"
}

resource "aws_service_discovery_private_dns_namespace" "server" {
  name = "server.local"
  description = "server.local"
  vpc = data.aws_vpc.vpc.id
}

