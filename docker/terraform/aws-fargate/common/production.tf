locals {
  production_availability_zones = ["us-east-1a", "us-east-1b"]
}

provider "aws" {
  region = var.region
}

#resource "aws_key_pair" "key" {
#  key_name   = "production_key"
#  public_key = "${file("production_key.pub")}"
#}

module "net" {
  source               = "./modules/net"
  environment          = "production"
  vpc_cidr             = "10.0.0.0/16"
  public_subnets_cidr  = ["10.0.1.0/24", "10.0.2.0/24"]
  private_subnets_cidr = ["10.0.10.0/24", "10.0.20.0/24"]
  region               = var.region
  availability_zones   = local.production_availability_zones
  key_name             = "production_key"
}

module "ecs" {
  source             = "./modules/ecs"
  environment        = "production"
  vpc_id             = module.net.vpc_id
  availability_zones = local.production_availability_zones
  repository_name_core = "core/production"
  repository_name_icore = "icore/production"
  subnets_ids        = flatten(module.net.private_subnets_id)
  public_subnet_ids  =  flatten([ module.net.public_subnets_id ])
  security_groups_ids = module.net.security_groups_ids
}
