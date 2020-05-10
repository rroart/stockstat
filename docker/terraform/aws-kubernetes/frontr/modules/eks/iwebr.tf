resource "kubernetes_deployment" "iwebr" {
  metadata {
    name = "iwebr"

    labels = {
      app = "iwebr"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "iwebr"
      }
    }

    template {
      metadata {
        labels = {
          app = "iwebr"
        }
      }

      spec {
        container {
          name  = "iwebr"
          image = var.repository_url_iwebr

          port {
            container_port = 80
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "iwebr" {
  metadata {
    name = "iwebr"

    labels = {
      app = "iwebr"
    }
  }

  spec {
    port {
      protocol    = "TCP"
      port        = 80
      target_port = "80"
    }

    selector = {
      app = "iwebr"
    }

    session_affinity = "None"
  }
}

resource "kubernetes_ingress" "name_virtual_host_ingress_iwebr" {
  metadata {
    name = "name-virtual-host-ingress-iwebr"
  }

  spec {
    rule {
      host = var.MYIWEB

      http {
        path {
          backend {
            service_name = "iwebr"
            service_port = "80"
          }
        }
      }
    }
  }
}

