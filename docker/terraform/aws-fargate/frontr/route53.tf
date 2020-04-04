resource "aws_route53_delegation_set" "main" {
  reference_name = "DynDNS"
}

data "aws_route53_zone" "primary_route" {
  name              = var.domain
}

resource "aws_route53_record" "iwww-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "www.icli.${var.domain}"
  type    = "A"

  alias {
    name                   = module.ecs.alb_dns_name_iwebr
    zone_id                = module.ecs.alb_zone_id_iwebr
    evaluate_target_health = true
  }
}

resource "aws_route53_record" "www-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "www.${var.domain}"
  type    = "A"

  alias {
    name                   = module.ecs.alb_dns_name_webr
    zone_id                = module.ecs.alb_zone_id_webr
    evaluate_target_health = true
  }
}

