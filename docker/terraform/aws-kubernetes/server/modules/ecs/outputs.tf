output "repository_url_core" {
  value = aws_ecr_repository.stockstat_app_core.repository_url
}

output "repository_url_icore" {
  value = aws_ecr_repository.stockstat_app_icore.repository_url
}

output "repository_url_iwebcore" {
  value = aws_ecr_repository.stockstat_app_iwebcore.repository_url
}

output "cluster_name" {
  value = data.aws_ecs_cluster.cluster.cluster_name
}

output "security_group_id" {
  value = data.aws_security_group.ecs_service.id
}

