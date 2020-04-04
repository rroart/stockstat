/*====
Cloudwatch Log Group
======*/
resource "aws_cloudwatch_log_group" "webr" {
  name = "webr"

  tags = {
    Environment = var.environment
    Application = "Core"
  }
}

resource "aws_cloudwatch_log_group" "iwebr" {
  name = "iwebr"

  tags = {
    Environment = var.environment
    Application = "iCore"
  }
}

/*====
ECR repository to store our Docker images
======*/
resource "aws_ecr_repository" "stockstat_app_webr" {
  name = var.repository_name_webr
}

resource "aws_ecr_repository" "stockstat_app_iwebr" {
  name = var.repository_name_iwebr
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

/* the task definition for the iwebr service */
data "template_file" "webr_task" {
  template = file("${path.module}/tasks/webr_task_definition.json")

  vars = {
    image           = aws_ecr_repository.stockstat_app_webr.repository_url
    log_group       = aws_cloudwatch_log_group.webr.name
    fargate_cpu    = var.fargate_cpu
    fargate_memory = var.fargate_memory
    aws_region     = var.aws_region
    app_port       = 80
  }
}

data "template_file" "iwebr_task" {
  template = file("${path.module}/tasks/iwebr_task_definition.json")

  vars = {
    image           = aws_ecr_repository.stockstat_app_iwebr.repository_url
    log_group       = aws_cloudwatch_log_group.iwebr.name
    fargate_cpu    = var.fargate_cpu
    fargate_memory = var.fargate_memory
    aws_region     = var.aws_region
    app_port       = var.app_port
  }
}

resource "aws_ecs_task_definition" "iwebr" {
  family                   = "${var.environment}_iwebr"
  container_definitions    = data.template_file.iwebr_task.rendered
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = data.aws_iam_role.ecs_execution_role.arn
  task_role_arn            = data.aws_iam_role.ecs_execution_role.arn
}

resource "aws_ecs_task_definition" "webr" {
  family                   = "${var.environment}_webr"
  container_definitions    = data.template_file.webr_task.rendered
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
resource "random_id" "target_group_suffix_iwebr" {
  byte_length = 2
}

resource "random_id" "target_group_suffix_webr" {
  byte_length = 2
}

resource "aws_alb_target_group" "alb_target_group_iwebr" {
  name        = "${var.environment}-alb-target-group-${random_id.target_group_suffix_iwebr.hex}"
  port        = 80
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [ aws_alb.alb_iwebr ]
}

resource "aws_alb_target_group" "alb_target_group_webr" {
  name        = "${var.environment}-alb-target-group-${random_id.target_group_suffix_webr.hex}"
  port        = 80
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [ aws_alb.alb_webr ]
}

/* security group for ALB */
resource "aws_security_group" "iwebr_inbound_sg" {
  name        = "${var.environment}-iwebr-inbound-sg"
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
    Name = "${var.environment}-iwebr-inbound-sg"
  }
}

resource "aws_security_group" "webr_inbound_sg" {
  name        = "${var.environment}-webr-inbound-sg"
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
    Name = "${var.environment}-webr-inbound-sg"
  }
}

resource "aws_alb" "alb_iwebr" {
  name            = "${var.environment}-alb-iwebr"
  subnets         = var.public_subnet_ids
  security_groups = flatten([ var.security_groups_ids, aws_security_group.iwebr_inbound_sg.id ])

  tags = {
    Name        = "${var.environment}-alb-iwebr"
    Environment = var.environment
  }
}

resource "aws_alb" "alb_webr" {
  name            = "${var.environment}-alb-webr"
  subnets         = var.public_subnet_ids
  security_groups = flatten([ var.security_groups_ids, aws_security_group.webr_inbound_sg.id ])

  tags = {
    Name        = "${var.environment}-alb-webr"
    Environment = var.environment
  }
}

resource "aws_alb_listener" "iwebr" {
  load_balancer_arn = aws_alb.alb_iwebr.arn
  port              = "80"
  protocol          = "HTTP"
  depends_on        = [aws_alb_target_group.alb_target_group_iwebr]

  default_action {
    target_group_arn = aws_alb_target_group.alb_target_group_iwebr.arn
    type             = "forward"
  }
}

resource "aws_alb_listener" "webr" {
  load_balancer_arn = aws_alb.alb_webr.arn
  port              = "80"
  protocol          = "HTTP"
  depends_on        = [aws_alb_target_group.alb_target_group_webr]

  default_action {
    target_group_arn = aws_alb_target_group.alb_target_group_webr.arn
    type             = "forward"
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
data "aws_ecs_task_definition" "iwebr" {
  task_definition = aws_ecs_task_definition.iwebr.family
  depends_on      = [aws_ecs_task_definition.iwebr]
}

data "aws_ecs_task_definition" "webr" {
  task_definition = aws_ecs_task_definition.webr.family
  depends_on      = [aws_ecs_task_definition.webr]
}

resource "aws_ecs_service" "iwebr" {
  name = "${var.environment}-iwebr"
  task_definition = "${aws_ecs_task_definition.iwebr.family}:${max(
    aws_ecs_task_definition.iwebr.revision,
    data.aws_ecs_task_definition.iwebr.revision,
  )}"
  desired_count = 2
  launch_type   = "FARGATE"
  cluster       = data.aws_ecs_cluster.cluster.id
  depends_on    = [aws_iam_role_policy.ecs_service_role_policy, aws_alb_target_group.alb_target_group_iwebr]

  network_configuration {
    security_groups = flatten([ var.security_groups_ids, data.aws_security_group.ecs_service.id ])
    subnets         = var.subnets_ids
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.alb_target_group_iwebr.arn
    container_name   = "iwebr"
    container_port   = "80"
  }
  #depends_on = ["aws_alb_target_group.alb_target_group"]
}

resource "aws_ecs_service" "webr" {
  name = "${var.environment}-webr"
  task_definition = "${aws_ecs_task_definition.webr.family}:${max(
    aws_ecs_task_definition.webr.revision,
    data.aws_ecs_task_definition.webr.revision,
  )}"
  desired_count = 2
  launch_type   = "FARGATE"
  cluster       = data.aws_ecs_cluster.cluster.id
  depends_on    = [aws_iam_role_policy.ecs_service_role_policy, aws_alb_target_group.alb_target_group_webr]

  network_configuration {
    security_groups = flatten([ var.security_groups_ids, data.aws_security_group.ecs_service.id ])
    subnets         = var.subnets_ids
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.alb_target_group_webr.arn
    container_name   = "webr"
    container_port   = "80"
  }
  #depends_on = ["aws_alb_target_group.alb_target_group"]
}

/*====
Auto Scaling for ECS
======*/

data "aws_iam_role" "ecs_autoscale_role" {
  name               = "${var.environment}_ecs_autoscale_role"
}

resource "aws_iam_role_policy" "ecs_autoscale_role_policy" {
  name   = "ecs_autoscale_role_policy"
   policy = file("${path.module}/../../../common/modules/ecs/policies/ecs-autoscale-role-policy.json")
  role   = data.aws_iam_role.ecs_autoscale_role.id
}

resource "aws_appautoscaling_target" "target" {
  service_namespace  = "ecs"
  resource_id        = "service/${data.aws_ecs_cluster.cluster.cluster_name}/${aws_ecs_service.iwebr.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  role_arn           = data.aws_iam_role.ecs_autoscale_role.arn
  min_capacity       = 2
  max_capacity       = 4
}

resource "aws_appautoscaling_policy" "up" {
  name               = "${var.environment}_scale_up"
  service_namespace  = "ecs"
  resource_id        = "service/${data.aws_ecs_cluster.cluster.cluster_name}/${aws_ecs_service.iwebr.name}"
  scalable_dimension = "ecs:service:DesiredCount"

  step_scaling_policy_configuration {
    adjustment_type         = "ChangeInCapacity"
    cooldown                = 60
    metric_aggregation_type = "Maximum"

    step_adjustment {
      metric_interval_lower_bound = 0
      scaling_adjustment          = 1
    }
  }

  depends_on = [aws_appautoscaling_target.target]
}

resource "aws_appautoscaling_policy" "down" {
  name               = "${var.environment}_scale_down"
  service_namespace  = "ecs"
  resource_id        = "service/${data.aws_ecs_cluster.cluster.cluster_name}/${aws_ecs_service.iwebr.name}"
  scalable_dimension = "ecs:service:DesiredCount"

  step_scaling_policy_configuration {
    adjustment_type         = "ChangeInCapacity"
    cooldown                = 60
    metric_aggregation_type = "Maximum"

    step_adjustment {
      metric_interval_lower_bound = 0
      scaling_adjustment          = -1
    }
  }

  depends_on = [aws_appautoscaling_target.target]
}

/* metric used for auto scale */
resource "aws_cloudwatch_metric_alarm" "service_cpu_high" {
  alarm_name          = "${var.environment}_webr_cpu_utilization_high"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = "60"
  statistic           = "Maximum"
  threshold           = "85"

  dimensions = {
    ClusterName = data.aws_ecs_cluster.cluster.cluster_name
    ServiceName = aws_ecs_service.iwebr.name
  }

  alarm_actions = [aws_appautoscaling_policy.up.arn]
  ok_actions    = [aws_appautoscaling_policy.down.arn]
}

