/*====
Cloudwatch Log Group
======*/
resource "aws_cloudwatch_log_group" "pytorch" {
  name = "pytorch"

  tags = {
    Environment = var.environment
    Application = "Core"
  }
}

/*====
ECR repository to store our Docker images
======*/
resource "aws_ecr_repository" "stockstat_app_pytorch" {
  name = var.repository_name_pytorch
}

/* the task definition for the tensorflow service */
data "template_file" "pytorch_task" {
  template = file("${path.module}/tasks/pytorch_task_definition.json")

  vars = {
    image           = aws_ecr_repository.stockstat_app_pytorch.repository_url
    log_group       = aws_cloudwatch_log_group.pytorch.name
    fargate_cpu    = var.fargate_cpu
    fargate_memory = var.fargate_memory
    aws_region     = var.aws_region
    app_port       = 80
  }
}

resource "aws_ecs_task_definition" "pytorch" {
  family                   = "${var.environment}_pytorch"
  container_definitions    = data.template_file.pytorch_task.rendered
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = data.aws_iam_role.ecs_execution_role.arn
  task_role_arn            = data.aws_iam_role.ecs_execution_role.arn
}

resource "random_id" "target_group_suffix_pytorch" {
  byte_length = 2
}

resource "aws_alb_target_group" "alb_target_group_pytorch" {
  name        = "${var.environment}-alb-target-group-${random_id.target_group_suffix_pytorch.hex}"
  port        = 80
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [ aws_alb.alb_pytorch ]
}

resource "aws_security_group" "pytorch_inbound_sg" {
  name        = "${var.environment}-pytorch-inbound-sg"
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
    Name = "${var.environment}-pytorch-inbound-sg"
  }
}

resource "aws_alb" "alb_pytorch" {
  name            = "${var.environment}-alb-pytorch"
  subnets         = var.public_subnet_ids
  security_groups = flatten([ var.security_groups_ids, aws_security_group.pytorch_inbound_sg.id ])

  tags = {
    Name        = "${var.environment}-alb-pytorch"
    Environment = var.environment
  }
}

resource "aws_alb_listener" "pytorch" {
  load_balancer_arn = aws_alb.alb_pytorch.arn
  port              = "80"
  protocol          = "HTTP"
  depends_on        = [aws_alb_target_group.alb_target_group_pytorch]

  default_action {
    target_group_arn = aws_alb_target_group.alb_target_group_pytorch.arn
    type             = "forward"
  }
}

data "aws_ecs_task_definition" "pytorch" {
  task_definition = aws_ecs_task_definition.pytorch.family
  depends_on      = [aws_ecs_task_definition.pytorch]
}

resource "aws_ecs_service" "pytorch" {
  name = "${var.environment}-pytorch"
  task_definition = "${aws_ecs_task_definition.pytorch.family}:${max(
    aws_ecs_task_definition.pytorch.revision,
    data.aws_ecs_task_definition.pytorch.revision,
  )}"
  desired_count = 1
  launch_type   = "FARGATE"
  cluster       = data.aws_ecs_cluster.cluster.id
  depends_on    = [aws_iam_role_policy.ecs_service_role_policy, aws_alb_target_group.alb_target_group_pytorch]

  network_configuration {
    security_groups = flatten([ var.security_groups_ids, data.aws_security_group.ecs_service.id ])
    subnets         = var.subnets_ids
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.alb_target_group_pytorch.arn
    container_name   = "pytorch"
    container_port   = "80"
  }

  service_registries {
    registry_arn = aws_service_discovery_service.pt.arn
  }

  #depends_on = ["aws_alb_target_group.alb_target_group"]
}

/* metric used for auto scale */
resource "aws_cloudwatch_metric_alarm" "service_cpu_high" {
  alarm_name          = "${var.environment}_pytorch_cpu_utilization_high"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = "60"
  statistic           = "Maximum"
  threshold           = "85"

  dimensions = {
    ClusterName = data.aws_ecs_cluster.cluster.cluster_name
    ServiceName = aws_ecs_service.tensorflow.name
  }

  alarm_actions = [aws_appautoscaling_policy.up.arn]
  ok_actions    = [aws_appautoscaling_policy.down.arn]
}

resource "aws_service_discovery_service" "pt" {
  name = var.MYPTSERVERLOCAL
  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.ml.id
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

