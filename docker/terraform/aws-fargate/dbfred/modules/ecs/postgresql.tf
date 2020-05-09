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
resource "aws_ecr_repository" "stockstat_app_postgresql" {
  name = var.repository_name_postgresql
}

/*====
ECS task definitions
======*/

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

/*====
App Load Balancer
======*/
resource "random_id" "target_group_suffix_postgresql" {
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

/* Simply specify the family to find the latest ACTIVE revision in that family */
data "aws_ecs_task_definition" "postgresql" {
  task_definition = aws_ecs_task_definition.postgresql.family
  depends_on      = [aws_ecs_task_definition.postgresql]
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

