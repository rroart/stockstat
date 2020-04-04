resource "aws_route53_delegation_set" "main" {
  reference_name = "DynDNS"
}

resource "aws_route53_zone" "primary_route" {
  name              = var.domain
  delegation_set_id = aws_route53_delegation_set.main.id
}
