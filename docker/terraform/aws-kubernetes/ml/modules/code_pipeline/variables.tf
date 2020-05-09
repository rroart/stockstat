variable "repository_url_pytorch" {
  description = "The url of the ECR repository"
}

variable "repository_url_tensorflow" {
  description = "The url of the ECR repository"
}

variable "region" {
  description = "The region to use"
}

variable "ecs_cluster_name" {
  description = "The cluster that we will deploy"
}

variable "run_task_subnet_id" {
  description = "The subnet Id where single run task will be executed"
}

variable "run_task_security_group_ids" {
  type        = list(string)
  description = "The security group Ids attached where the single run task will be executed"
}

variable "pytorch" {
  description = "Core Server"
  default = "http://pytorch.stockstat.tk/"
}

variable "tensorflow" {
  description = "iCore Server"
  default = "http://tensorflow.stockstat.tk"
}

variable "MYSERVER" {}

variable "MYISERVER" {}