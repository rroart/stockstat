resource "aws_iam_role" "codepipeline_role" {
  name               = "codepipeline-role"

  assume_role_policy = file("${path.module}/policies/codepipeline_role.json")
}

/*
/* CodeBuild
*/
resource "aws_iam_role" "codebuild_role" {
  name               = "codebuild-role"
  assume_role_policy = file("${path.module}/policies/codebuild_role.json")
}
