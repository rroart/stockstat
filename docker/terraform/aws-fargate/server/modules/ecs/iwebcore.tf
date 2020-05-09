resource "aws_cloudwatch_log_group" "iwebcore" {
  name = "iwebcore"

  tags = {
    Environment = var.environment
    Application = "iWebCore"
  }
}

resource "aws_ecr_repository" "stockstat_app_iwebcore" {
  name = var.repository_name_iwebcore
}

data "template_file" "iwebcore_task" {
  template = file("${path.module}/tasks/iwebcore_task_definition.json")

  vars = {
    image           = aws_ecr_repository.stockstat_app_iwebcore.repository_url
    log_group       = aws_cloudwatch_log_group.iwebcore.name
    fargate_cpu    = var.fargate_cpu
    fargate_memory = var.fargate_memory
    aws_region     = var.aws_region
    app_port       = var.app_port
  }
}

resource "aws_ecs_task_definition" "iwebcore" {
  family                   = "${var.environment}_iwebcore"
  container_definitions    = data.template_file.iwebcore_task.rendered
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = data.aws_iam_role.ecs_execution_role.arn
  task_role_arn            = data.aws_iam_role.ecs_execution_role.arn
}

resource "random_id" "target_group_suffix_iwebcore" {
  byte_length = 2
}

resource "aws_alb_target_group" "alb_target_group_iwebcore" {
  name        = "${var.environment}-alb-target-group-${random_id.target_group_suffix_iwebcore.hex}"
  port        = 80
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [ aws_alb.alb_iwebcore ]
}

resource "aws_security_group" "iwebcore_inbound_sg" {
  name        = "${var.environment}-iwebcore-inbound-sg"
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
    Name = "${var.environment}-iwebcore-inbound-sg"
  }
}

resource "aws_alb" "alb_iwebcore" {
  name            = "${var.environment}-alb-iwebcore"
  subnets         = var.public_subnet_ids
  security_groups = flatten([ var.security_groups_ids, aws_security_group.iwebcore_inbound_sg.id ])

  tags = {
    Name        = "${var.environment}-alb-iwebcore"
    Environment = var.environment
  }
}

resource "aws_alb_listener" "iwebcore" {
  load_balancer_arn = aws_alb.alb_iwebcore.arn
  port              = "80"
  protocol          = "HTTP"
  depends_on        = [aws_alb_target_group.alb_target_group_iwebcore]

  default_action {
    target_group_arn = aws_alb_target_group.alb_target_group_iwebcore.arn
    type             = "forward"
  }
}

data "aws_ecs_task_definition" "iwebcore" {
  task_definition = aws_ecs_task_definition.iwebcore.family
  depends_on      = [aws_ecs_task_definition.iwebcore]
}

resource "aws_ecs_service" "iwebcore" {
  name = "${var.environment}-iwebcore"
  task_definition = "${aws_ecs_task_definition.iwebcore.family}:${max(
    aws_ecs_task_definition.iwebcore.revision,
    data.aws_ecs_task_definition.iwebcore.revision,
  )}"
  desired_count = 1
  launch_type   = "FARGATE"
  cluster       = data.aws_ecs_cluster.cluster.id
  depends_on    = [aws_iam_role_policy.ecs_service_role_policy, aws_alb_target_group.alb_target_group_iwebcore]

  network_configuration {
    security_groups = flatten([ var.security_groups_ids, data.aws_security_group.ecs_service.id ])
    subnets         = var.subnets_ids
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.alb_target_group_iwebcore.arn
    container_name   = "iwebcore"
    container_port   = "80"
  }
  #depends_on = ["aws_alb_target_group.alb_target_group"]
}

