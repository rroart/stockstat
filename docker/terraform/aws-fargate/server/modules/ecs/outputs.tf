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

output "service_name_iwebcore" {
  value = aws_ecs_service.iwebcore.name
}

output "service_name_icore" {
  value = aws_ecs_service.icore.name
}

output "service_name_core" {
  value = aws_ecs_service.core.name
}

output "alb_dns_name_iwebcore" {
  value = aws_alb.alb_iwebcore.dns_name
}

output "alb_zone_id_iwebcore" {
  value = aws_alb.alb_iwebcore.zone_id
}

output "alb_dns_name_icore" {
  value = aws_alb.alb_icore.dns_name
}

output "alb_zone_id_icore" {
  value = aws_alb.alb_icore.zone_id
}

output "alb_dns_name_core" {
  value = aws_alb.alb_core.dns_name
}

output "alb_zone_id_core" {
  value = aws_alb.alb_core.zone_id
}

output "security_group_id" {
  value = data.aws_security_group.ecs_service.id
}

