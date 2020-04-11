variable "region" {
  description = "Region that the instances will be created"
}

/*====
environment specific variables
======*/

variable "domain" {
  default = "The domain of your application"
}

variable "MYCORESERVERLOCAL" {}

variable "MYICORESERVERLOCAL" {}

variable "MYASERVERLOCALFQDN" {}

variable "MYISERVERLOCALFQDN" {}

variable "MYSERVERLOCALFQDN" {}

variable "MYDBSERVERLOCALFQDN" {}

variable "MYCONFIG" {}
variable "MYICONFIG" {}
variable "MYENV" {}
variable "MYIENV" {}
