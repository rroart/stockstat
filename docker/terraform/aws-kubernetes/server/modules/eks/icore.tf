resource "kubernetes_deployment" "icore" {
  metadata {
    name = "icore"

    labels = {
      app = "icore"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "icore"
      }
    }

    template {
      metadata {
        labels = {
          app = "icore"
        }
      }

      spec {
        container {
          name  = "icore"
          image = var.repository_url_icore

          port {
            container_port = 80
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "icore" {
  metadata {
    name = "icore"

    labels = {
      app = "icore"
    }
  }

  spec {
    port {
      protocol    = "TCP"
      port        = 80
      target_port = "80"
    }

    selector = {
      app = "icore"
    }

    session_affinity = "None"
  }
}

resource "kubernetes_ingress" "name_virtual_host_ingress_icore" {
  metadata {
    name = "name-virtual-host-ingress-icore"

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
            service_name = "icore"
            service_port = "80"
          }
        }
      }
    }
  }
}

