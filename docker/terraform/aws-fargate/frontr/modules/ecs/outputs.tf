output "repository_url_webr" {
  value = aws_ecr_repository.stockstat_app_webr.repository_url
}

output "repository_url_iwebr" {
  value = aws_ecr_repository.stockstat_app_iwebr.repository_url
}

output "cluster_name" {
  value = data.aws_ecs_cluster.cluster.cluster_name
}

output "service_name_iwebr" {
  value = aws_ecs_service.iwebr.name
}

output "service_name_webr" {
  value = aws_ecs_service.webr.name
}

output "alb_dns_name_iwebr" {
  value = aws_alb.alb_iwebr.dns_name
}

output "alb_zone_id_iwebr" {
  value = aws_alb.alb_iwebr.zone_id
}

output "alb_dns_name_webr" {
  value = aws_alb.alb_webr.dns_name
}

output "alb_zone_id_webr" {
  value = aws_alb.alb_webr.zone_id
}

output "security_group_id" {
  value = data.aws_security_group.ecs_service.id
}

