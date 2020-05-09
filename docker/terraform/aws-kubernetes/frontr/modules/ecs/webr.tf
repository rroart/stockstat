/*====
ECR repository to store our Docker images
======*/
resource "aws_ecr_repository" "stockstat_app_webr" {
  name = var.repository_name_webr
}

