resource "aws_s3_bucket" "source-webr" {
  bucket        = "stockstat-source"
  acl           = "private"
  force_destroy = true
}

/* policies */
data "template_file" "codepipeline_policy_webr" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codepipeline.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-webr.arn
  }
}

resource "aws_iam_role_policy" "codepipeline_policy_webr" {
  name   = "codepipeline_policy"
  role   = data.aws_iam_role.codepipeline_role.id
  policy = data.template_file.codepipeline_policy_webr.rendered
}

/*
/* CodeBuild
*/
data "template_file" "codebuild_policy_webr" {
  template = file("${path.module}/../../../common/modules/code_pipeline/policies/codebuild_policy.json")

  vars = {
    aws_s3_bucket_arn = aws_s3_bucket.source-webr.arn
  }
}

resource "aws_iam_role_policy" "codebuild_policy_webr" {
  name        = "codebuild-policy-webr"
  role        = data.aws_iam_role.codebuild_role.id
  policy      = data.template_file.codebuild_policy_webr.rendered
}

data "template_file" "buildspecwebr" {
  template = file("${path.module}/buildspecwebr.yml")

  vars = {
    repository_url     = var.repository_url_webr
    region             = var.region
    cluster_name       = var.ecs_cluster_name
    subnet_id          = var.run_task_subnet_id
    security_group_ids = join(",", var.run_task_security_group_ids)
    myserver      = var.MYSERVER
  }
}

resource "aws_codebuild_project" "webr_build" {
  name          = "webr-codebuild"
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
    buildspec = data.template_file.buildspecwebr.rendered
  }
}

/* CodePipeline */

resource "aws_codepipeline" "pipeline_webr" {
  name     = "webr-pipeline"
  role_arn = data.aws_iam_role.codepipeline_role.arn

  artifact_store {
    location = aws_s3_bucket.source-webr.bucket
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
        ProjectName = "webr-codebuild"
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
        ServiceName = var.ecs_service_name_webr
        FileName    = "imagedefinitions.json"
      }
    }
  }
}

