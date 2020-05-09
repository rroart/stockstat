data "aws_route53_zone" "primary_route2" {
  name              = var.domain
}

resource "aws_route53_record" "iwebr-prod" {
  zone_id = data.aws_route53_zone.primary_route2.id
  name    = "iwebr.${var.domain}"
  type    = "CNAME"
  ttl     = "300"
  records = ["${kubernetes_service.iwebr.load_balancer_ingress.0.hostname}"]
}

