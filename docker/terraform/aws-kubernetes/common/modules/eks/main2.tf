resource "aws_eks_node_group" "production-cluster" {
  cluster_name    = var.cluster-name
  node_group_name = var.environment
  node_role_arn   = aws_iam_role.production-group.arn
  subnet_ids      = var.public_subnet_ids

  scaling_config {
    desired_size = 1
    max_size     = 1
    min_size     = 1
  }

  # Ensure that IAM Role permissions are created before and deleted after EKS Node Group handling.
  # Otherwise, EKS will not be able to properly delete EC2 Instances and Elastic Network Interfaces.
  depends_on = [
    aws_iam_role_policy_attachment.production-AmazonEKSWorkerNodePolicy,
    aws_iam_role_policy_attachment.production-AmazonEKS_CNI_Policy,
    aws_iam_role_policy_attachment.production-AmazonEC2ContainerRegistryReadOnly,
    aws_eks_cluster.cluster
  ]
}

resource "aws_iam_role" "production-group" {
  name = "eks-node-group-demo"

  assume_role_policy = jsonencode({
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
    Version = "2012-10-17"
  })
}

resource "aws_iam_role_policy_attachment" "production-AmazonEKSWorkerNodePolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.production-group.name
}

resource "aws_iam_role_policy_attachment" "production-AmazonEKS_CNI_Policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.production-group.name
}

resource "aws_iam_role_policy_attachment" "production-AmazonEC2ContainerRegistryReadOnly" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.production-group.name
}
