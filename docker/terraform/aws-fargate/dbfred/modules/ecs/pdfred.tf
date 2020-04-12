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

resource "aws_ecr_repository" "stockstat_app_pdfred" {
  name = var.repository_name_pdfred
}

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

resource "random_id" "target_group_suffix_pdfred" {
  byte_length = 2
}

data "aws_ecs_task_definition" "pdfred" {
  task_definition = aws_ecs_task_definition.pdfred.family
  depends_on      = [aws_ecs_task_definition.pdfred]
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
