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

resource "gitlab_project" "stockstat" {
  name        = "stockstat"
  description = "Stockstat project"
  visibility_level = "public"
}
