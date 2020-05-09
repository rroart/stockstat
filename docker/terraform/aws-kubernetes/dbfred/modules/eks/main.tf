data "aws_eks_cluster" "cluster" {
  name            = var.cluster-name
}

data "aws_eks_cluster_auth" "cluster_auth" {
  name = var.cluster-name
}

provider "kubernetes" {
  host                   = data.aws_eks_cluster.cluster.endpoint
  cluster_ca_certificate = base64decode(data.aws_eks_cluster.cluster.certificate_authority.0.data)
  token                  = data.aws_eks_cluster_auth.cluster_auth.token
  load_config_file       = false
}

