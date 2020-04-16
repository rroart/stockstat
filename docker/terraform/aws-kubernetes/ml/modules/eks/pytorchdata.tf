resource "kubernetes_persistent_volume" "pytorchdatapv" {
  metadata {
    name = "pytorchdatapv"
  }

  spec {
    capacity = {
      storage = "1G"
    }

    access_modes       = ["ReadWriteMany"]
    storage_class_name = "manual"
    persistent_volume_source {
      local {
        path = "/data/pytorch"
      }
    }
  }
}

resource "kubernetes_persistent_volume_claim" "pytorchdatapv" {
  metadata {
    name = "pytorchdatapv"
  }

  spec {
    access_modes = ["ReadWriteMany"]

    resources {
      requests = {
        storage = "1G"
      }
    }
    volume_name = kubernetes_persistent_volume.pytorchdatapv.metadata.0.name
  }
}

