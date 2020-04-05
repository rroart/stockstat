output "repository_url_pdfred" {
  value = aws_ecr_repository.stockstat_app_pdfred.repository_url
}

output "repository_url_postgresql" {
  value = aws_ecr_repository.stockstat_app_postgresql.repository_url
}

output "cluster_name" {
  value = data.aws_ecs_cluster.cluster.cluster_name
}

output "service_name_postgresql" {
  value = aws_ecs_service.postgresql.name
}

output "service_name_pdfred" {
  value = aws_ecs_service.pdfred.name
}

output "security_group_id" {
  value = data.aws_security_group.ecs_service.id
}

