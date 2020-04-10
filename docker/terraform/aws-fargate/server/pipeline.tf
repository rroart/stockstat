
module "code_pipeline" {
  source                      = "./modules/code_pipeline"
  repository_url_core         = module.ecs.repository_url_core
  repository_url_icore         = module.ecs.repository_url_icore
  region                      = var.region
  ecs_service_name_icore        = module.ecs.service_name_icore
  ecs_service_name_core       = module.ecs.service_name_core
  ecs_cluster_name            = module.ecs.cluster_name
  run_task_subnet_id          = module.net.private_subnets_id[0]
  run_task_security_group_ids = flatten([ module.net.security_groups_ids, module.ecs.security_group_id])
  MYSERVERLOCALFQDN	      = var.MYSERVERLOCALFQDN
  MYDBSERVERLOCALFQDN	      = var.MYDBSERVERLOCALFQDN
  MYCONFIG		      = var.MYCONFIG
  MYICONFIG		      = var.MYICONFIG
  MYENV			      = var.MYENV
  MYIENV		      = var.MYIENV
}
