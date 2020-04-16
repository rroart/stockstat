data "aws_iam_role" "codepipeline_role" {
  name               = "codepipeline-role"
}

data "aws_iam_role" "codebuild_role" {
  name               = "codebuild-role"
}

