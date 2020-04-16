resource "kubernetes_deployment" "iwebcore" {
  metadata {
    name = "iwebcore"

    labels = {
      app = "iwebcore"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "iwebcore"
      }
    }

    template {
      metadata {
        labels = {
          app = "iwebcore"
        }
      }

      spec {
        container {
          name  = "iwebcore"
          image = var.repository_url_iwebcore

          port {
            container_port = 80
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "iwebcore" {
  metadata {
    name = "iwebcore"

    labels = {
      app = "iwebcore"
    }
  }

  spec {
    port {
      protocol    = "TCP"
      port        = 80
      target_port = "80"
    }

    selector = {
      app = "iwebcore"
    }

    session_affinity = "None"
  }
}

resource "kubernetes_ingress" "name_virtual_host_ingress_iwebcore" {
  metadata {
    name = "name-virtual-host-ingress-iwebcore"

    annotations = {
      "kubernetes.io/ingress.class" = "nginx"

      "nginx.ingress.kubernetes.io/proxy-read-timeout" = "1800"

      "nginx.ingress.kubernetes.io/proxy-send-timeout" = "1800"
    }
  }

  spec {
    rule {
      host = "core.icli.stockstat.tk"

      http {
        path {
          backend {
            service_name = "iwebcore"
            service_port = "80"
          }
        }
      }
    }
  }
}

