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
}

resource "gitlab_project" "stockstat" {
  name        = "stockstat"
  description = "Stockstat project"
  visibility_level = "public"
  shared_runners_enabled = true
  merge_method = ff
}
