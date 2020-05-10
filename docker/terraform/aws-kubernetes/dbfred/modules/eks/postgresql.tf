resource "kubernetes_deployment" "postgresql_12_centos_7" {
  metadata {
    name = "postgresql-12-centos7"

    labels = {
      app = "postgresql-12-centos7"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "postgresql-12-centos7"
      }
    }

    template {
      metadata {
        labels = {
          app = "postgresql-12-centos7"
        }
      }

      spec {
        container {
          name  = "postgresql-12-centos7"
          image = "centos/postgresql-12-centos7"

          port {
            container_port = 5432
          }

          env {
            name  = "POSTGRESQL_USER"
            value = "stockstat"
          }

          env {
            name  = "POSTGRESQL_PASSWORD"
            value = "password"
          }

          env {
            name  = "POSTGRESQL_DATABASE"
            value = "stockstat"
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "postgresql_12_centos_7" {
  metadata {
    name = "postgresql-12-centos7"

    labels = {
      app = "postgresql-12-centos7"
    }
  }

  spec {
    port {
      protocol    = "TCP"
      port        = 5432
      target_port = "5432"
    }

    selector = {
      app = "postgresql-12-centos7"
    }

    session_affinity = "None"
  }
}

resource "kubernetes_ingress" "name_virtual_host_ingress_psql" {
  metadata {
    name = "name-virtual-host-ingress-psql"
  }

  spec {
    rule {
      host = "postgresql-12-centos7.stockstat.tk"

      http {
        path {
          backend {
            service_name = "postgresql-12-centos7"
            service_port = "5432"
          }
        }
      }
    }
  }
}

