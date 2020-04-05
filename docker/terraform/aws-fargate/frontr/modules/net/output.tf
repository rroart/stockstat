output "vpc_id" {
  value = data.aws_vpc.vpc.id
}

output "public_subnets_id" {
  value = [data.aws_subnet.public_subnet.*.id]
}

output "private_subnets_id" {
  value = data.aws_subnet.private_subnet.*.id
}

output "default_sg_id" {
  value = data.aws_security_group.default.id
}

output "security_groups_ids" {
  value = [data.aws_security_group.default.id]
}

