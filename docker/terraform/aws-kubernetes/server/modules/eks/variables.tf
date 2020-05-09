variable "cluster-name" {
  default = "stockstat"
  type    = string
}

variable "MYIP" {}

variable "MYACCOUNT" {}

variable "environment" {
  description = "The environment"
}

variable "vpc_id" {
  description = "The environment"
}

variable "public_subnet_ids" {
  type        = list(string)
  description = "The private subnets to use"
}

variable "repository_url_core" {
  description = "The url of the ECR repository"
}

variable "repository_url_icore" {
  description = "The url of the ECR repository"
}

variable "repository_url_iwebcore" {
  description = "The url of the ECR repository"
}

