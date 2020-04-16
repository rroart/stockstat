resource "kubernetes_deployment" "pytorch" {
  metadata {
    name = "pytorch"

    labels = {
      app = "pytorch"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "pytorch"
      }
    }

    template {
      metadata {
        labels = {
          app = "pytorch"
        }
      }

      spec {
        volume {
          name = "pytorchdatavol"

          persistent_volume_claim {
            claim_name = "pytorchdatapv"
          }
        }

        init_container {
          name    = "pytorchinit"
          image   = "pytorchdata"
          command = ["/bin/sh", "-c", "cp -R /data/* /newdata/"]

          volume_mount {
            name       = "pytorchdatavol"
            mount_path = "/newdata"
          }
        }

        container {
          name  = "pytorch"
          image = var.repository_url_pytorch

          port {
            container_port = 80
          }

          volume_mount {
            name       = "pytorchdatavol"
            mount_path = "/data"
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "pytorch" {
  metadata {
    name = "pytorch"

    labels = {
      app = "pytorch"
    }
  }

  spec {
    port {
      protocol    = "TCP"
      port        = 80
      target_port = "80"
    }

    selector = {
      app = "pytorch"
    }

    session_affinity = "None"
  }
}

resource "kubernetes_ingress" "name_virtual_host_ingress_pt" {
  metadata {
    name = "name-virtual-host-ingress-pt"
  }

  spec {
    rule {
      host = "pt.stockstat.tk"

      http {
        path {
          backend {
            service_name = "pytorch"
            service_port = "80"
          }
        }
      }
    }
  }
}

