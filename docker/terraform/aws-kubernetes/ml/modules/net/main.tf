/*====
The VPC
======*/

data "aws_vpc" "vpc" {
  #default = true
  filter {
    name = "tag:Name"
    values = ["${var.environment}-vpc"]
  }
}

/*====
Subnets
======*/

/* Public subnet */
data "aws_subnet" "public_subnet" {
  count                   = length(var.public_subnets_cidr)
  filter {
    name = "tag:Name"
    values = ["${var.environment}-${element(var.availability_zones, count.index)}-public-subnet"]
  }
  #vpc_id                  = data.aws_vpc.vpc.id
  #default_for_az          = true
  #availability_zone       = element(var.availability_zones, count.index)
}

/* Private subnet */
data "aws_subnet" "private_subnet" {
  count                   = length(var.private_subnets_cidr)
  filter {
    name = "tag:Name"
    values = ["${var.environment}-${element(var.availability_zones, count.index)}-private-subnet"]
  }
  #vpc_id                  = data.aws_vpc.vpc.id
  #default_for_az          = true
  #availability_zone       = element(var.availability_zones, count.index)
}


/*====
VPC's Default Security Group
======*/
data "aws_security_group" "default" {
  name        = "${var.environment}-default-sg"
  #vpc_id      = data.aws_vpc.vpc.id
}

