variable "repository_url_pdfred" {
  description = "The url of the ECR repository"
}

#variable "repository_url_postgresql" {
#  description = "The url of the ECR repository"
#}

variable "region" {
  description = "The region to use"
}

variable "ecs_cluster_name" {
  description = "The cluster that we will deploy"
}

#variable "ecs_service_name_postgresql" {
#  description = "The ECS service that will be deployed"
#}

#variable "ecs_service_name_pdfred" {
#  description = "The ECS service that will be deployed"
#}

variable "run_task_subnet_id" {
  description = "The subnet Id where single run task will be executed"
}

variable "run_task_security_group_ids" {
  type        = list(string)
  description = "The security group Ids attached where the single run task will be executed"
}

variable "pdfred" {
  description = "Core Server"
  default = "http://pdfred.stockstat.tk/"
}

variable "postgresql" {
  description = "iCore Server"
  default = "http://postgresql.stockstat.tk"
}

variable "MYSERVER" {}

variable "MYISERVER" {}

variable "MYDBSERVERLOCALFQDN" {}
