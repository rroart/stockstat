output "repository_url_pytorch" {
  value = aws_ecr_repository.stockstat_app_pytorch.repository_url
}

output "repository_url_tensorflow" {
  value = aws_ecr_repository.stockstat_app_tensorflow.repository_url
}

output "cluster_name" {
  value = data.aws_ecs_cluster.cluster.cluster_name
}

output "service_name_tensorflow" {
  value = aws_ecs_service.tensorflow.name
}

output "service_name_pytorch" {
  value = aws_ecs_service.pytorch.name
}

output "alb_dns_name_tensorflow" {
  value = aws_alb.alb_tensorflow.dns_name
}

output "alb_zone_id_tensorflow" {
  value = aws_alb.alb_tensorflow.zone_id
}

output "alb_dns_name_pytorch" {
  value = aws_alb.alb_pytorch.dns_name
}

output "alb_zone_id_pytorch" {
  value = aws_alb.alb_pytorch.zone_id
}

output "security_group_id" {
  value = data.aws_security_group.ecs_service.id
}

