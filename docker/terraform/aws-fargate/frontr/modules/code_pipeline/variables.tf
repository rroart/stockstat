variable "repository_url_webr" {
  description = "The url of the ECR repository"
}

variable "repository_url_iwebr" {
  description = "The url of the ECR repository"
}

variable "region" {
  description = "The region to use"
}

variable "ecs_cluster_name" {
  description = "The cluster that we will deploy"
}

variable "ecs_service_name_iwebr" {
  description = "The ECS service that will be deployed"
}

variable "ecs_service_name_webr" {
  description = "The ECS service that will be deployed"
}

variable "run_task_subnet_id" {
  description = "The subnet Id where single run task will be executed"
}

variable "run_task_security_group_ids" {
  type        = list(string)
  description = "The security group Ids attached where the single run task will be executed"
}

variable "webr" {
  description = "Core Server"
  default = "http://webr.stockstat.tk/"
}

variable "iwebr" {
  description = "iCore Server"
  default = "http://iwebr.stockstat.tk"
}

variable "MYSERVER" {}

variable "MYISERVER" {}