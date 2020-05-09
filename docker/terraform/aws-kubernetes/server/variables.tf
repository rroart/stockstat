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

variable "MYSERVERLOCALFQDN" {}

variable "MYASERVERLOCALFQDN" {}

variable "MYISERVERLOCALFQDN" {}

variable "MYDBSERVERLOCAL" {}

variable "MYDBSERVERLOCALFQDN" {}

variable "cluster-name" {
  default = "stockstat"
  type    = string
}

variable "MYIP" {}

variable "MYACCOUNT" {}

variable "MYCONFIG" {}
variable "MYICONFIG" {}
variable "MYENV" {}
variable "MYIENV" {}
