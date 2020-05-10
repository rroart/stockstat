variable "cluster-name" {
  default = "stockstat"
  type    = string
}

variable "MYWEB" {}
variable "MYIWEB" {}

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

variable "repository_url_webr" {
  description = "The url of the ECR repository"
}

variable "repository_url_iwebr" {
  description = "The url of the ECR repository"
}

variable "domain" {}