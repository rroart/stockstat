output "kubernetes_service_core_hostname" {
  value = kubernetes_service.core.load_balancer_ingress.0.hostname
}

output "kubernetes_service_icore_hostname" {
  value = kubernetes_service.icore.load_balancer_ingress.0.hostname
}

output "kubernetes_service_iwebcore_hostname" {
  value = kubernetes_service.iwebcore.load_balancer_ingress.0.hostname
}

