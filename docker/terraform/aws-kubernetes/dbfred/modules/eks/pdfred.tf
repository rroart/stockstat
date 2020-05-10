resource "kubernetes_deployment" "pdfred" {
  metadata {
    name = "pdfred"

    labels = {
      app = "pdfred"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "pdfred"
      }
    }

    template {
      metadata {
        labels = {
          app = "pdfred"
        }
      }

      spec {
        container {
          name  = "pdfred"
          image = var.repository_url_pdfred
        }
      }
    }
  }
}

