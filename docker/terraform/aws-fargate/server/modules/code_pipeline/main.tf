resource "aws_s3_bucket" "source-core" {
  bucket        = "stockstat-source"
  acl           = "private"
  force_destroy = true
}

resource "aws_s3_bucket" "source-icore" {
  bucket        = "stockstat-source"
  acl           = "private"
  force_destroy = true
}

resource "aws_s3_bucket" "source-iwebcore" {
  bucket        = "stockstat-source"
  acl           = "private"
  force_destroy = true
}

data "aws_iam_role" "codepipeline_role" {
  name               = "codepipeline-role"
}

/* policies */
data "template_file" "codepipeline_policy_core" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codepipeline.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-core.arn
  }
}

data "template_file" "codepipeline_policy_icore" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codepipeline.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-icore.arn
  }
}

data "template_file" "codepipeline_policy_iwebcore" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codepipeline.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-iwebcore.arn
  }
}

resource "aws_iam_role_policy" "codepipeline_policy_core" {
  name   = "codepipeline_policy"
  role   = data.aws_iam_role.codepipeline_role.id
  policy = data.template_file.codepipeline_policy_core.rendered
}

resource "aws_iam_role_policy" "codepipeline_policy_icore" {
  name   = "codepipeline_policy"
  role   = data.aws_iam_role.codepipeline_role.id
  policy = data.template_file.codepipeline_policy_icore.rendered
}

resource "aws_iam_role_policy" "codepipeline_policy_iwebcore" {
  name   = "codepipeline_policy"
  role   = data.aws_iam_role.codepipeline_role.id
  policy = data.template_file.codepipeline_policy_iwebcore.rendered
}

/*
/* CodeBuild
*/
data "aws_iam_role" "codebuild_role" {
  name               = "codebuild-role"
}

data "template_file" "codebuild_policy_core" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codebuild_policy.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-core.arn
  }
}

data "template_file" "codebuild_policy_icore" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codebuild_policy.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-icore.arn
  }
}

data "template_file" "codebuild_policy_iwebcore" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codebuild_policy.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-iwebcore.arn
  }
}

resource "aws_iam_role_policy" "codebuild_policy_core" {
  name        = "codebuild-policy-core"
  role        = data.aws_iam_role.codebuild_role.id
  policy      = data.template_file.codebuild_policy_core.rendered
}

resource "aws_iam_role_policy" "codebuild_policy_icore" {
  name        = "codebuild-policy-icore"
  role        = data.aws_iam_role.codebuild_role.id
  policy      = data.template_file.codebuild_policy_icore.rendered
}

resource "aws_iam_role_policy" "codebuild_policy_iwebcore" {
  name        = "codebuild-policy-icore"
  role        = data.aws_iam_role.codebuild_role.id
  policy      = data.template_file.codebuild_policy_iwebcore.rendered
}

data "template_file" "buildspeccore" {
  template = file("${path.module}/buildspeccore.yml")

  vars = {
    repository_url     = var.repository_url_core
    region             = var.region
    cluster_name       = var.ecs_cluster_name
    subnet_id          = var.run_task_subnet_id
    security_group_ids = join(",", var.run_task_security_group_ids)
    mydbserver         = var.MYDBSERVERLOCALFQDN
    myconfig	       = var.MYCONFIG
    myenv	       = var.MYENV
  }
}

data "template_file" "buildspecicore" {
  template = file("${path.module}/buildspecicore.yml")

  vars = {
    repository_url     = var.repository_url_icore
    region             = var.region
    cluster_name       = var.ecs_cluster_name
    subnet_id          = var.run_task_subnet_id
    security_group_ids = join(",", var.run_task_security_group_ids)
    myserver           = var.MYSERVERLOCALFQDN
    mydbserver         = var.MYDBSERVERLOCALFQDN
    myconfig	       = var.MYICONFIG
    myenv	       = var.MYIENV
  }
}

data "template_file" "buildspeciwebcore" {
  template = file("${path.module}/buildspeciwebcore.yml")

  vars = {
    repository_url     = var.repository_url_iwebcore
    region             = var.region
    cluster_name       = var.ecs_cluster_name
    subnet_id          = var.run_task_subnet_id
    security_group_ids = join(",", var.run_task_security_group_ids)
    myaserver           = var.MYASERVERLOCALFQDN
    myiserver           = var.MYISERVERLOCALFQDN
    myserver           = var.MYSERVERLOCALFQDN
    mydbserver         = var.MYDBSERVERLOCALFQDN
    myconfig	       = var.MYICONFIG
    myenv	       = var.MYIENV
  }
}

resource "aws_codebuild_project" "core_build" {
  name          = "core-codebuild"
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
    buildspec = data.template_file.buildspeccore.rendered
  }
}

resource "aws_codebuild_project" "icore_build" {
  name          = "icore-codebuild"
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
    buildspec = data.template_file.buildspecicore.rendered
  }
}

resource "aws_codebuild_project" "iwebcore_build" {
  name          = "iwebcore-codebuild"
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
    buildspec = data.template_file.buildspeciwebcore.rendered
  }
}

/* CodePipeline */

resource "aws_codepipeline" "pipeline_core" {
  name     = "core-pipeline"
  role_arn = data.aws_iam_role.codepipeline_role.arn

  artifact_store {
    location = aws_s3_bucket.source-core.bucket
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
        ProjectName = "core-codebuild"
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
        ServiceName = var.ecs_service_name_core
        FileName    = "imagedefinitions.json"
      }
    }
  }
}

resource "aws_codepipeline" "pipeline-icore" {
  name     = "icore-pipeline"
  role_arn = data.aws_iam_role.codepipeline_role.arn

  artifact_store {
    location = aws_s3_bucket.source-icore.bucket
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
        ProjectName = "icore-codebuild"
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
        ServiceName = var.ecs_service_name_icore
        FileName    = "imagedefinitions.json"
      }
    }
  }
}

resource "aws_codepipeline" "pipeline-iwebcore" {
  name     = "iwebcore-pipeline"
  role_arn = data.aws_iam_role.codepipeline_role.arn

  artifact_store {
    location = aws_s3_bucket.source-iwebcore.bucket
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
        ProjectName = "iwebcore-codebuild"
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
        ServiceName = var.ecs_service_name_iwebcore
        FileName    = "imagedefinitions.json"
      }
    }
  }
}

