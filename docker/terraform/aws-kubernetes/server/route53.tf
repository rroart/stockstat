resource "aws_route53_delegation_set" "main" {
  reference_name = "DynDNS"
}

data "aws_route53_zone" "primary_route" {
  name              = var.domain
}

resource "aws_route53_record" "iwebcore-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "iwebcore.${var.domain}"
  type    = "CNAME"
  ttl     = "300"
  records = [ module.eks.kubernetes_service_iwebcore_hostname ]
}

resource "aws_route53_record" "icore-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "icore.${var.domain}"
  type    = "CNAME"
  ttl     = "300"
  records = [ module.eks.kubernetes_service_icore_hostname ]
}

resource "aws_route53_record" "core-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "core.${var.domain}"
  type    = "CNAME"
  ttl     = "300"
  records = [ module.eks.kubernetes_service_core_hostname ]
}

