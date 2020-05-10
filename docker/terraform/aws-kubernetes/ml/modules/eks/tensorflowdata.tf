resource "kubernetes_persistent_volume" "tensorflowdatapv" {
  metadata {
    name = "tensorflowdatapv"
  }

  spec {
    capacity = {
      storage = "1G"
    }

    access_modes       = ["ReadWriteMany"]
    storage_class_name = "manual"
    persistent_volume_source {
      local {
        path = "/data/tensorflow"
      }
    }
  }
}

resource "kubernetes_persistent_volume_claim" "tensorflowdatapv" {
  metadata {
    name = "tensorflowdatapv"
  }

  spec {
    access_modes = ["ReadWriteMany"]

    resources {
      requests = {
        storage = "1G"
      }
    }
    volume_name = kubernetes_persistent_volume.tensorflowdatapv.metadata.0.name
  }
}

