variable "region" {
  description = "Region that the instances will be created"
}

/*====
environment specific variables
======*/

variable "domain" {
  default = "The domain of your application"
}

variable "MYSERVER" {}

variable "MYISERVER" {}

variable "MYASERVERLOCALFQDN" {}

variable "cluster-name" {
  default = "stockstat"
  type    = string
}

variable "MYWEB" {}
variable "MYIWEB" {}

variable "MYIP" {}

variable "MYACCOUNT" {}