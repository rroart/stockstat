resource "aws_route53_delegation_set" "main" {
  reference_name = "DynDNS"
}

data "aws_route53_zone" "primary_route" {
  name              = var.domain
}

resource "aws_route53_record" "iwebcore-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "webcore.icli.${var.domain}"
  type    = "A"

  alias {
    name                   = module.ecs.alb_dns_name_iwebcore
    zone_id                = module.ecs.alb_zone_id_iwebcore
    evaluate_target_health = true
  }
}

resource "aws_route53_record" "icore-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "core.icli.${var.domain}"
  type    = "A"

  alias {
    name                   = module.ecs.alb_dns_name_icore
    zone_id                = module.ecs.alb_zone_id_icore
    evaluate_target_health = true
  }
}

resource "aws_route53_record" "core-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "core.${var.domain}"
  type    = "A"

  alias {
    name                   = module.ecs.alb_dns_name_core
    zone_id                = module.ecs.alb_zone_id_core
    evaluate_target_health = true
  }
}

