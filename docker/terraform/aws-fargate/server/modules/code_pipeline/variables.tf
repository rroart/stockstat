variable "repository_url_core" {
  description = "The url of the ECR repository"
}

variable "repository_url_icore" {
  description = "The url of the ECR repository"
}

variable "region" {
  description = "The region to use"
}

variable "ecs_cluster_name" {
  description = "The cluster that we will deploy"
}

variable "ecs_service_name_icore" {
  description = "The ECS service that will be deployed"
}

variable "ecs_service_name_core" {
  description = "The ECS service that will be deployed"
}

variable "run_task_subnet_id" {
  description = "The subnet Id where single run task will be executed"
}

variable "run_task_security_group_ids" {
  type        = list(string)
  description = "The security group Ids attached where the single run task will be executed"
}

variable "core" {
  description = "Core Server"
  default = "http://core.stockstat.tk/"
}

variable "icore" {
  description = "iCore Server"
  default = "http://icore.stockstat.tk"
}

variable "MYSERVERLOCALFQDN" {}

variable "MYDBSERVERLOCALFQDN" {}

variable "MYCONFIG" {}
variable "MYICONFIG" {}
variable "MYENV" {}
variable "MYIENV" {}
