variable "cluster-name" {
  type = string
}

variable "public_subnet_ids" {
  type        = list(string)
  description = "The private subnets to use"
}

#variable "default_sg_id" {}

variable "environment" {}

variable "MYIP" {}

variable "MYACCOUNT" {}

variable "vpc_id" {}