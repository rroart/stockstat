output "cluster_name" {
  value = aws_ecs_cluster.cluster.name
}

output "security_group_id" {
  value = aws_security_group.ecs_service.id
}

