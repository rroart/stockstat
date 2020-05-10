data "aws_route53_zone" "primary_route" {
  name              = var.domain
}

resource "aws_route53_record" "webr-prod" {
  zone_id = data.aws_route53_zone.primary_route.id
  name    = "webr.${var.domain}"
  type    = "CNAME"
  ttl     = "300"
  records = ["${kubernetes_service.webr.load_balancer_ingress.0.hostname}"]
}

