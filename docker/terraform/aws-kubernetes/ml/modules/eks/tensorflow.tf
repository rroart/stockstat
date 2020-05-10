resource "kubernetes_deployment" "tensorflow" {
  metadata {
    name = "tensorflow"

    labels = {
      app = "tensorflow"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "tensorflow"
      }
    }

    template {
      metadata {
        labels = {
          app = "tensorflow"
        }
      }

      spec {
        volume {
          name = "tensorflowdatavol"

          persistent_volume_claim {
            claim_name = "tensorflowdatapv"
          }
        }

        init_container {
          name    = "tensorflowinit"
          image   = "tensorflowdata"
          command = ["/bin/sh", "-c", "cp -R /data/* /newdata/"]

          volume_mount {
            name       = "tensorflowdatavol"
            mount_path = "/newdata"
          }
        }

        container {
          name  = "tensorflow"
          image = var.repository_url_tensorflow

          port {
            container_port = 80
          }

          volume_mount {
            name       = "tensorflowdatavol"
            mount_path = "/data"
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "tensorflow" {
  metadata {
    name = "tensorflow"

    labels = {
      app = "tensorflow"
    }
  }

  spec {
    port {
      protocol    = "TCP"
      port        = 80
      target_port = "80"
    }

    selector = {
      app = "tensorflow"
    }

    session_affinity = "None"
  }
}

resource "kubernetes_ingress" "name_virtual_host_ingress_tf" {
  metadata {
    name = "name-virtual-host-ingress-tf"
  }

  spec {
    rule {
      host = "tf.stockstat.tk"

      http {
        path {
          backend {
            service_name = "tensorflow"
            service_port = "80"
          }
        }
      }
    }
  }
}

