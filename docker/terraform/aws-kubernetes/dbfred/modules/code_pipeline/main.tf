data "aws_iam_role" "codepipeline_role" {
  name               = "codepipeline-role"
}

/*
/* CodeBuild
*/
data "aws_iam_role" "codebuild_role" {
  name               = "codebuild-role"
}

