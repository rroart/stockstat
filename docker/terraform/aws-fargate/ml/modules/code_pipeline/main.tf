resource "aws_s3_bucket" "source-pytorch" {
  bucket        = "stockstat-source"
  acl           = "private"
  force_destroy = true
}

resource "aws_s3_bucket" "source-tensorflow" {
  bucket        = "stockstat-source"
  acl           = "private"
  force_destroy = true
}

data "aws_iam_role" "codepipeline_role" {
  name               = "codepipeline-role"
}

/* policies */
data "template_file" "codepipeline_policy_pytorch" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codepipeline.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-pytorch.arn
  }
}

data "template_file" "codepipeline_policy_tensorflow" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codepipeline.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-tensorflow.arn
  }
}

resource "aws_iam_role_policy" "codepipeline_policy_pytorch" {
  name   = "codepipeline_policy"
  role   = data.aws_iam_role.codepipeline_role.id
  policy = data.template_file.codepipeline_policy_pytorch.rendered
}

resource "aws_iam_role_policy" "codepipeline_policy_tensorflow" {
  name   = "codepipeline_policy"
  role   = data.aws_iam_role.codepipeline_role.id
  policy = data.template_file.codepipeline_policy_tensorflow.rendered
}

/*
/* CodeBuild
*/
data "aws_iam_role" "codebuild_role" {
  name               = "codebuild-role"
}

data "template_file" "codebuild_policy_pytorch" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codebuild_policy.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-pytorch.arn
  }
}

data "template_file" "codebuild_policy_tensorflow" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codebuild_policy.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-tensorflow.arn
  }
}

resource "aws_iam_role_policy" "codebuild_policy_pytorch" {
  name        = "codebuild-policy-pytorch"
  role        = data.aws_iam_role.codebuild_role.id
  policy      = data.template_file.codebuild_policy_pytorch.rendered
}

resource "aws_iam_role_policy" "codebuild_policy_tensorflow" {
  name        = "codebuild-policy-tensorflow"
  role        = data.aws_iam_role.codebuild_role.id
  policy      = data.template_file.codebuild_policy_tensorflow.rendered
}

data "template_file" "buildspecpytorch" {
  template = file("${path.module}/buildspecpytorch.yml")

  vars = {
    repository_url     = var.repository_url_pytorch
    region             = var.region
    cluster_name       = var.ecs_cluster_name
    subnet_id          = var.run_task_subnet_id
    security_group_ids = join(",", var.run_task_security_group_ids)
  }
}

data "template_file" "buildspectensorflow" {
  template = file("${path.module}/buildspectensorflow.yml")

  vars = {
    repository_url     = var.repository_url_tensorflow
    region             = var.region
    cluster_name       = var.ecs_cluster_name
    subnet_id          = var.run_task_subnet_id
    security_group_ids = join(",", var.run_task_security_group_ids)
  }
}

resource "aws_codebuild_project" "pytorch_build" {
  name          = "pytorch-codebuild"
  build_timeout = "10"
  service_role  = data.aws_iam_role.codebuild_role.arn

  artifacts {
    type = "CODEPIPELINE"
  }

  environment {
    compute_type    = "BUILD_GENERAL1_SMALL"
    // https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-available.html
    image           = "aws/codebuild/docker:17.09.0"
    type            = "LINUX_CONTAINER"
    privileged_mode = true
  }

  source {
    type      = "CODEPIPELINE"
    buildspec = data.template_file.buildspecpytorch.rendered
  }
}

resource "aws_codebuild_project" "tensorflow_build" {
  name          = "tensorflow-codebuild"
  build_timeout = "10"
  service_role  = data.aws_iam_role.codebuild_role.arn

  artifacts {
    type = "CODEPIPELINE"
  }

  environment {
    compute_type    = "BUILD_GENERAL1_SMALL"
    // https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-available.html
    image           = "aws/codebuild/docker:17.09.0"
    type            = "LINUX_CONTAINER"
    privileged_mode = true
  }

  source {
    type      = "CODEPIPELINE"
    buildspec = data.template_file.buildspectensorflow.rendered
  }
}

/* CodePipeline */

resource "aws_codepipeline" "pipeline_pytorch" {
  name     = "pytorch-pipeline"
  role_arn = data.aws_iam_role.codepipeline_role.arn

  artifact_store {
    location = aws_s3_bucket.source-pytorch.bucket
    type     = "S3"
  }

  stage {
    name = "Source"

    action {
      name             = "Source"
      category         = "Source"
      owner            = "ThirdParty"
      provider         = "GitHub"
      version          = "1"
      output_artifacts = ["source"]

      configuration = {
        Owner      = "rroart"
        Repo       = "stockstat"
        Branch     = "master"
	OAuthToken = var.github_token
      }
    }
  }

  stage {
    name = "Build"

    action {
      name             = "Build"
      category         = "Build"
      owner            = "AWS"
      provider         = "CodeBuild"
      version          = "1"
      input_artifacts  = ["source"]
      output_artifacts = ["imagedefinitions"]

      configuration = {
        ProjectName = "pytorch-codebuild"
      }
    }
  }

  stage {
    name = "Production"

    action {
      name            = "Deploy"
      category        = "Deploy"
      owner           = "AWS"
      provider        = "ECS"
      input_artifacts = ["imagedefinitions"]
      version         = "1"

      configuration = {
        ClusterName = var.ecs_cluster_name
        ServiceName = var.ecs_service_name_pytorch
        FileName    = "imagedefinitions.json"
      }
    }
  }
}

resource "aws_codepipeline" "pipeline-tensorflow" {
  name     = "tensorflow-pipeline"
  role_arn = data.aws_iam_role.codepipeline_role.arn

  artifact_store {
    location = aws_s3_bucket.source-tensorflow.bucket
    type     = "S3"
  }

  stage {
    name = "Source"

    action {
      name             = "Source"
      category         = "Source"
      owner            = "ThirdParty"
      provider         = "GitHub"
      version          = "1"
      output_artifacts = ["source"]

      configuration = {
        Owner      = "rroart"
        Repo       = "stockstat"
        Branch     = "master"
	OAuthToken = var.github_token
      }
    }
  }

  stage {
    name = "Build"

    action {
      name             = "Build"
      category         = "Build"
      owner            = "AWS"
      provider         = "CodeBuild"
      version          = "1"
      input_artifacts  = ["source"]
      output_artifacts = ["imagedefinitions"]

      configuration = {
        ProjectName = "tensorflow-codebuild"
      }
    }
  }

  stage {
    name = "Production"

    action {
      name            = "Deploy"
      category        = "Deploy"
      owner           = "AWS"
      provider        = "ECS"
      input_artifacts = ["imagedefinitions"]
      version         = "1"

      configuration = {
        ClusterName = var.ecs_cluster_name
        ServiceName = var.ecs_service_name_tensorflow
        FileName    = "imagedefinitions.json"
      }
    }
  }
}

