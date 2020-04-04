resource "aws_route53_delegation_set" "main" {
  reference_name = "DynDNS"
}

data "aws_route53_zone" "primary_route" {
  name              = var.domain
}

resource "aws_route53_record" "tf-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "tf.${var.domain}"
  type    = "A"

  alias {
    name                   = module.ecs.alb_dns_name_tensorflow
    zone_id                = module.ecs.alb_zone_id_tensorflow
    evaluate_target_health = true
  }
}

resource "aws_route53_record" "pt-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "pt.${var.domain}"
  type    = "A"

  alias {
    name                   = module.ecs.alb_dns_name_pytorch
    zone_id                = module.ecs.alb_zone_id_pytorch
    evaluate_target_health = true
  }
}

