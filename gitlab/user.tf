terraform {
  required_providers {
    gitlab = {
      source = "gitlabhq/gitlab"
      version = "3.6.0"
    }
  }
}

provider "gitlab" {
    #GITLAB_TOKEN
    #GITLAB_BASE_URL 
    insecure = true
}


resource "gitlab_user" "stockstat" {
  name             = "Stockstat Gitlab"
  username         = "stockstat"
  password         = "stockstat"
  email            = "gitlab@user.create"
  is_admin         = true
  projects_limit   = 4
  can_create_group = false
  is_external      = true
  reset_password   = false
}

resource "gitlab_project" "stockstatproject" {
  name        = "stockstat project"
  description = "Stockstat project"
  visibility_level = "public"
}

resource "gitlab_project_membership" "stockstatprojectmember" {
  project_id   = resource.gitlab_project.stockstatproject.id
  user_id      = resource.gitlab_user.stockstat.id
  access_level = "maintainer"
}
