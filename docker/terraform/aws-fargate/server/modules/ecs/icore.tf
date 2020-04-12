resource "aws_cloudwatch_log_group" "icore" {
  name = "icore"

  tags = {
    Environment = var.environment
    Application = "iCore"
  }
}

resource "aws_ecr_repository" "stockstat_app_icore" {
  name = var.repository_name_icore
}

data "template_file" "icore_task" {
  template = file("${path.module}/tasks/icore_task_definition.json")

  vars = {
    image           = aws_ecr_repository.stockstat_app_icore.repository_url
    log_group       = aws_cloudwatch_log_group.icore.name
    fargate_cpu    = var.fargate_cpu
    fargate_memory = var.fargate_memory
    aws_region     = var.aws_region
    app_port       = var.app_port
  }
}

resource "aws_ecs_task_definition" "icore" {
  family                   = "${var.environment}_icore"
  container_definitions    = data.template_file.icore_task.rendered
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = data.aws_iam_role.ecs_execution_role.arn
  task_role_arn            = data.aws_iam_role.ecs_execution_role.arn
}

resource "random_id" "target_group_suffix_icore" {
  byte_length = 2
}

resource "aws_alb_target_group" "alb_target_group_icore" {
  name        = "${var.environment}-alb-target-group-${random_id.target_group_suffix_icore.hex}"
  port        = 80
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [ aws_alb.alb_icore ]
}

resource "aws_security_group" "icore_inbound_sg" {
  name        = "${var.environment}-icore-inbound-sg"
  description = "Allow HTTP from Anywhere into ALB"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 80
    to_port     = 80
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
    Name = "${var.environment}-icore-inbound-sg"
  }
}

resource "aws_alb" "alb_icore" {
  name            = "${var.environment}-alb-icore"
  subnets         = var.public_subnet_ids
  security_groups = flatten([ var.security_groups_ids, aws_security_group.icore_inbound_sg.id ])

  tags = {
    Name        = "${var.environment}-alb-icore"
    Environment = var.environment
  }
}

resource "aws_alb_listener" "icore" {
  load_balancer_arn = aws_alb.alb_icore.arn
  port              = "80"
  protocol          = "HTTP"
  depends_on        = [aws_alb_target_group.alb_target_group_icore]

  default_action {
    target_group_arn = aws_alb_target_group.alb_target_group_icore.arn
    type             = "forward"
  }
}

data "aws_ecs_task_definition" "icore" {
  task_definition = aws_ecs_task_definition.icore.family
  depends_on      = [aws_ecs_task_definition.icore]
}

resource "aws_ecs_service" "icore" {
  name = "${var.environment}-icore"
  task_definition = "${aws_ecs_task_definition.icore.family}:${max(
    aws_ecs_task_definition.icore.revision,
    data.aws_ecs_task_definition.icore.revision,
  )}"
  desired_count = 1
  launch_type   = "FARGATE"
  cluster       = data.aws_ecs_cluster.cluster.id
  depends_on    = [aws_iam_role_policy.ecs_service_role_policy, aws_alb_target_group.alb_target_group_icore]

  network_configuration {
    security_groups = flatten([ var.security_groups_ids, data.aws_security_group.ecs_service.id ])
    subnets         = var.subnets_ids
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.alb_target_group_icore.arn
    container_name   = "icore"
    container_port   = "80"
  }

  service_registries {
    registry_arn = aws_service_discovery_service.iserver.arn
  }

  #depends_on = ["aws_alb_target_group.alb_target_group"]
}

resource "aws_service_discovery_service" "iserver" {
  name = var.MYICORESERVERLOCAL
  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.server.id
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

