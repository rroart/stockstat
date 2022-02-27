resource "kubernetes_deployment" "dbclone" {
  metadata {
    name = "dbclone"

    labels = {
      app = "dbclone"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "dbclone"
      }
    }

    template {
      metadata {
        labels = {
          app = "dbclone"
        }
      }

      spec {
        container {
          name  = "dbclone"
          image = "dbclone"
        }
      }
    }
  }
}

