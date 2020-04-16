resource "kubernetes_deployment" "webr" {
  metadata {
    name = "webr"

    labels = {
      app = "webr"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "webr"
      }
    }

    template {
      metadata {
        labels = {
          app = "webr"
        }
      }

      spec {
        container {
          name  = "webr"
          image = var.repository_url_iwebr

          port {
            container_port = 80
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "webr" {
  metadata {
    name = "webr"

    labels = {
      app = "webr"
    }
  }

  spec {
    port {
      protocol    = "TCP"
      port        = 80
      target_port = "80"
    }

    selector = {
      app = "webr"
    }

    session_affinity = "None"
  }
}

resource "kubernetes_ingress" "name_virtual_host_ingress_webr" {
  metadata {
    name = "name-virtual-host-ingress-webr"
  }

  spec {
    rule {
      host = var.MYWEB

      http {
        path {
          backend {
            service_name = "webr"
            service_port = "80"
          }
        }
      }
    }
  }
}

