/*====
Cloudwatch Log Group
======*/
resource "aws_cloudwatch_log_group" "pdfred" {
  name = "pdfred"

  tags = {
    Environment = var.environment
    Application = "Core"
  }
}

resource "aws_cloudwatch_log_group" "postgresql" {
  name = "postgresql"

  tags = {
    Environment = var.environment
    Application = "iCore"
  }
}

/*====
ECR repository to store our Docker images
======*/
resource "aws_ecr_repository" "stockstat_app_pdfred" {
  name = var.repository_name_pdfred
}

resource "aws_ecr_repository" "stockstat_app_postgresql" {
  name = var.repository_name_postgresql
}

/*====
ECS cluster
======*/
data "aws_ecs_cluster" "cluster" {
  cluster_name = "${var.environment}-ecs-cluster"
}

/*====
ECS task definitions
======*/

/* the task definition for the postgresql service */
data "template_file" "pdfred_task" {
  template = file("${path.module}/tasks/pdfred_task_definition.json")

  vars = {
    image           = aws_ecr_repository.stockstat_app_pdfred.repository_url
    log_group       = aws_cloudwatch_log_group.pdfred.name
    fargate_cpu    = var.fargate_cpu
    fargate_memory = var.fargate_memory
    aws_region     = var.aws_region
    app_port       = 80
  }
}

data "template_file" "postgresql_task" {
  template = file("${path.module}/tasks/postgresql_task_definition.json")

  vars = {
    #image           = aws_ecr_repository.stockstat_app_postgresql.repository_url
    #image           = "postgresql-12-centos7"
    image           = "centos/postgresql-12-centos7"
    log_group       = aws_cloudwatch_log_group.postgresql.name
    fargate_cpu    = var.fargate_cpu
    fargate_memory = var.fargate_memory
    aws_region     = var.aws_region
    app_port       = var.app_port
  }
}

resource "aws_ecs_task_definition" "postgresql" {
  family                   = "${var.environment}_postgresql"
  container_definitions    = data.template_file.postgresql_task.rendered
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = data.aws_iam_role.ecs_execution_role.arn
  task_role_arn            = data.aws_iam_role.ecs_execution_role.arn
}

resource "aws_ecs_task_definition" "pdfred" {
  family                   = "${var.environment}_pdfred"
  container_definitions    = data.template_file.pdfred_task.rendered
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = data.aws_iam_role.ecs_execution_role.arn
  task_role_arn            = data.aws_iam_role.ecs_execution_role.arn
}

/*====
App Load Balancer
======*/
resource "random_id" "target_group_suffix_postgresql" {
  byte_length = 2
}

resource "random_id" "target_group_suffix_pdfred" {
  byte_length = 2
}

/* security group for ALB */
resource "aws_security_group" "postgresql_inbound_sg" {
  name        = "${var.environment}-postgresql-inbound-sg"
  description = "Allow HTTP from Anywhere into ALB"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8
    to_port     = 0
    protocol    = "icmp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-postgresql-inbound-sg"
  }
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

/* Simply specify the family to find the latest ACTIVE revision in that family */
data "aws_ecs_task_definition" "postgresql" {
  task_definition = aws_ecs_task_definition.postgresql.family
  depends_on      = [aws_ecs_task_definition.postgresql]
}

data "aws_ecs_task_definition" "pdfred" {
  task_definition = aws_ecs_task_definition.pdfred.family
  depends_on      = [aws_ecs_task_definition.pdfred]
}
resource "aws_ecs_service" "postgresql" {
  name = "${var.environment}-postgresql"
  task_definition = "${aws_ecs_task_definition.postgresql.family}:${max(
    aws_ecs_task_definition.postgresql.revision,
    data.aws_ecs_task_definition.postgresql.revision,
  )}"
  desired_count = 1
  launch_type   = "FARGATE"
  cluster       = data.aws_ecs_cluster.cluster.id
  depends_on    = [aws_iam_role_policy.ecs_service_role_policy]

  network_configuration {
    security_groups = flatten([ var.security_groups_ids, data.aws_security_group.ecs_service.id ])
    subnets         = var.subnets_ids
  }

  service_registries {
    registry_arn = aws_service_discovery_service.db.arn
  }
}

resource "aws_ecs_service" "pdfred" {
  name = "${var.environment}-pdfred"
  task_definition = "${aws_ecs_task_definition.pdfred.family}:${max(
    aws_ecs_task_definition.pdfred.revision,
    data.aws_ecs_task_definition.pdfred.revision,
  )}"
  desired_count = 1
  launch_type   = "FARGATE"
  cluster       = data.aws_ecs_cluster.cluster.id
  depends_on    = [aws_iam_role_policy.ecs_service_role_policy]

  network_configuration {
    security_groups = flatten([ var.security_groups_ids, data.aws_security_group.ecs_service.id ])
    subnets         = var.subnets_ids
  }
}

/* Service discovery */


data "aws_vpc" "vpc" {
  #default = true
  filter {
    name = "tag:Name"
    values = ["${var.environment}-vpc"]
  }
}

resource "aws_service_discovery_private_dns_namespace" "db" {
  name = "db.local"
  description = "db.local"
  vpc = data.aws_vpc.vpc.id
}

resource "aws_service_discovery_service" "db" {
  name = var.MYDBSERVERLOCAL
  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.db.id
    dns_records {
      ttl = 10
      type = "A"
    }
    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}