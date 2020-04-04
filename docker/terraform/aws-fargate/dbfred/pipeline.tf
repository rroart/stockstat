
module "code_pipeline" {
  source                      = "./modules/code_pipeline"
  repository_url_pdfred         = module.ecs.repository_url_pdfred
  repository_url_postgresql         = module.ecs.repository_url_postgresql
  region                      = var.region
  ecs_service_name_postgresql        = module.ecs.service_name_postgresql
  ecs_service_name_pdfred       = module.ecs.service_name_pdfred
  ecs_cluster_name            = module.ecs.cluster_name
  run_task_subnet_id          = module.net.private_subnets_id[0]
  run_task_security_group_ids = flatten([ module.net.security_groups_ids, module.ecs.security_group_id])
  MYSERVER		      = var.MYSERVER
  MYISERVER		      = var.MYISERVER
  MYDBSERVERLOCALFQDN	      = var.MYDBSERVERLOCALFQDN
}
