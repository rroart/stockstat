resource "kubernetes_deployment" "core" {
  metadata {
    name = "core"

    labels = {
      app = "core"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "core"
      }
    }

    template {
      metadata {
        labels = {
          app = "core"
        }
      }

      spec {
        container {
          name  = "core"
          image = var.repository_url_core

          port {
            container_port = 80
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "core" {
  metadata {
    name = "core"

    labels = {
      app = "core"
    }
  }

  spec {
    port {
      protocol    = "TCP"
      port        = 80
      target_port = "80"
    }

    selector = {
      app = "core"
    }

    session_affinity = "None"
  }
}

resource "kubernetes_ingress" "name_virtual_host_ingress_core" {
  metadata {
    name = "name-virtual-host-ingress-core"

    annotations = {
      "kubernetes.io/ingress.class" = "nginx"

      "nginx.ingress.kubernetes.io/proxy-read-timeout" = "1800"

      "nginx.ingress.kubernetes.io/proxy-send-timeout" = "1800"
    }
  }

  spec {
    rule {
      host = "core.stockstat.tk"

      http {
        path {
          backend {
            service_name = "core"
            service_port = "80"
          }
        }
      }
    }
  }
}

