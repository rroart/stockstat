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
  email            = "gitlab@localhost"
  is_admin         = true
  projects_limit   = 4
  can_create_group = false
  is_external      = true
  reset_password   = false
}

resource "gitlab_project" "stockstat" {
  name        = "stockstat"
  description = "Stockstat project"
  visibility_level = "public"
  shared_runners_enabled = true
}

resource "gitlab_project_membership" "stockstatprojectmember" {
  project_id   = resource.gitlab_project.stockstat.id
  user_id      = resource.gitlab_user.stockstat.id
  access_level = "maintainer"
}
